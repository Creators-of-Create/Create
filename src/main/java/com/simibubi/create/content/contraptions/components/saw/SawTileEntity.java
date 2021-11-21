package com.simibubi.create.content.contraptions.components.saw;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.components.actors.BlockBreakingKineticTileEntity;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.AbstractBlockBreakQueue;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.recipe.RecipeConditions;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.ChorusPlantBlock;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.KelpTopBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StemGrownBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SawTileEntity extends BlockBreakingKineticTileEntity {

	private static final AxisAlignedBB RENDER_BOX = new AxisAlignedBB(0, 0, 0, 1, 1, 1);

	private static final Object cuttingRecipesKey = new Object();
	public static final LazyValue<IRecipeType<?>> woodcuttingRecipeType =
		new LazyValue<>(() -> Registry.RECIPE_TYPE.get(new ResourceLocation("druidcraft", "woodcutting")));

	public ProcessingInventory inventory;
	private int recipeIndex;
	private final LazyOptional<IItemHandler> invProvider;
	private FilteringBehaviour filtering;

	private ItemStack playEvent;

	public SawTileEntity(TileEntityType<? extends SawTileEntity> type) {
		super(type);
		inventory = new ProcessingInventory(this::start).withSlotLimit(!AllConfigs.SERVER.recipes.bulkCutting.get());
		inventory.remainingTime = -1;
		recipeIndex = 0;
		invProvider = LazyOptional.of(() -> inventory);
		playEvent = ItemStack.EMPTY;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		filtering = new FilteringBehaviour(this, new SawFilterSlot()).forRecipes();
		behaviours.add(filtering);
		behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnelsWhen(this::canProcess));
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.put("Inventory", inventory.serializeNBT());
		compound.putInt("RecipeIndex", recipeIndex);
		super.write(compound, clientPacket);

		if (!clientPacket || playEvent.isEmpty())
			return;
		compound.put("PlayEvent", playEvent.serializeNBT());
		playEvent = ItemStack.EMPTY;
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		recipeIndex = compound.getInt("RecipeIndex");
		if (compound.contains("PlayEvent"))
			playEvent = ItemStack.of(compound.getCompound("PlayEvent"));
	}

	@Override
	protected AxisAlignedBB makeRenderBoundingBox() {
		return RENDER_BOX.inflate(.125f)
			.move(worldPosition);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void tickAudio() {
		super.tickAudio();
		if (getSpeed() == 0)
			return;

		if (!playEvent.isEmpty()) {
			boolean isWood = false;
			Item item = playEvent.getItem();
			if (item instanceof BlockItem) {
				Block block = ((BlockItem) item).getBlock();
				isWood = block.getSoundType(block.defaultBlockState(), level, worldPosition, null) == SoundType.WOOD;
			}
			spawnEventParticles(playEvent);
			playEvent = ItemStack.EMPTY;
			if (!isWood)
				AllSoundEvents.SAW_ACTIVATE_STONE.playAt(level, worldPosition, 3, 1, true);
			else
				AllSoundEvents.SAW_ACTIVATE_WOOD.playAt(level, worldPosition, 3, 1, true);
			return;
		}
	}

	@Override
	public void tick() {
		if (shouldRun() && ticksUntilNextProgress < 0)
			destroyNextTick();
		super.tick();

		if (!canProcess())
			return;
		if (getSpeed() == 0)
			return;
		if (inventory.remainingTime == -1) {
			if (!inventory.isEmpty() && !inventory.appliedRecipe)
				start(inventory.getStackInSlot(0));
			return;
		}

		float processingSpeed = MathHelper.clamp(Math.abs(getSpeed()) / 24, 1, 128);
		inventory.remainingTime -= processingSpeed;

		if (inventory.remainingTime > 0)
			spawnParticles(inventory.getStackInSlot(0));

		if (inventory.remainingTime < 5 && !inventory.appliedRecipe) {
			if (level.isClientSide && !isVirtual())
				return;
			playEvent = inventory.getStackInSlot(0);
			applyRecipe();
			inventory.appliedRecipe = true;
			inventory.recipeDuration = 20;
			inventory.remainingTime = 20;
			sendData();
			return;
		}

		Vector3d itemMovement = getItemMovementVec();
		Direction itemMovementFacing = Direction.getNearest(itemMovement.x, itemMovement.y, itemMovement.z);
		if (inventory.remainingTime > 0)
			return;
		inventory.remainingTime = 0;

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;
			ItemStack tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE)
				.tryExportingToBeltFunnel(stack, itemMovementFacing.getOpposite(), false);
			if (tryExportingToBeltFunnel != null) {
				if (tryExportingToBeltFunnel.getCount() != stack.getCount()) {
					inventory.setStackInSlot(slot, tryExportingToBeltFunnel);
					notifyUpdate();
					return;
				}
				if (!tryExportingToBeltFunnel.isEmpty())
					return;
			}
		}

		BlockPos nextPos = worldPosition.offset(itemMovement.x, itemMovement.y, itemMovement.z);
		DirectBeltInputBehaviour behaviour = TileEntityBehaviour.get(level, nextPos, DirectBeltInputBehaviour.TYPE);
		if (behaviour != null) {
			boolean changed = false;
			if (!behaviour.canInsertFromSide(itemMovementFacing))
				return;
			if (level.isClientSide && !isVirtual())
				return;
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemStack stack = inventory.getStackInSlot(slot);
				if (stack.isEmpty())
					continue;
				ItemStack remainder = behaviour.handleInsertion(stack, itemMovementFacing, false);
				if (remainder.equals(stack, false))
					continue;
				inventory.setStackInSlot(slot, remainder);
				changed = true;
			}
			if (changed) {
				setChanged();
				sendData();
			}
			return;
		}

		// Eject Items
		Vector3d outPos = VecHelper.getCenterOf(worldPosition)
			.add(itemMovement.scale(.5f)
				.add(0, .5, 0));
		Vector3d outMotion = itemMovement.scale(.0625)
			.add(0, .125, 0);
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;
			ItemEntity entityIn = new ItemEntity(level, outPos.x, outPos.y, outPos.z, stack);
			entityIn.setDeltaMovement(outMotion);
			level.addFreshEntity(entityIn);
		}
		inventory.clear();
		level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
		inventory.remainingTime = -1;
		sendData();
	}

	@Override
	public void setRemoved() {
		invProvider.invalidate();
		super.setRemoved();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != Direction.DOWN)
			return invProvider.cast();
		return super.getCapability(cap, side);
	}

	protected void spawnEventParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		IParticleData particleData = null;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockParticleData(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock()
				.defaultBlockState());
		else
			particleData = new ItemParticleData(ParticleTypes.ITEM, stack);

		Random r = level.random;
		Vector3d v = VecHelper.getCenterOf(this.worldPosition)
			.add(0, 5 / 16f, 0);
		for (int i = 0; i < 10; i++) {
			Vector3d m = VecHelper.offsetRandomly(new Vector3d(0, 0.25f, 0), r, .125f);
			level.addParticle(particleData, v.x, v.y, v.z, m.x, m.y, m.y);
		}
	}

	protected void spawnParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		IParticleData particleData = null;
		float speed = 1;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockParticleData(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock()
				.defaultBlockState());
		else {
			particleData = new ItemParticleData(ParticleTypes.ITEM, stack);
			speed = .125f;
		}

		Random r = level.random;
		Vector3d vec = getItemMovementVec();
		Vector3d pos = VecHelper.getCenterOf(this.worldPosition);
		float offset = inventory.recipeDuration != 0 ? (float) (inventory.remainingTime) / inventory.recipeDuration : 0;
		offset /= 2;
		if (inventory.appliedRecipe)
			offset -= .5f;
		level.addParticle(particleData, pos.x() + -vec.x * offset, pos.y() + .45f, pos.z() + -vec.z * offset,
			-vec.x * speed, r.nextFloat() * speed, -vec.z * speed);
	}

	public Vector3d getItemMovementVec() {
		boolean alongX = !getBlockState().getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
		int offset = getSpeed() < 0 ? -1 : 1;
		return new Vector3d(offset * (alongX ? 1 : 0), 0, offset * (alongX ? 0 : -1));
	}

	private void applyRecipe() {
		List<? extends IRecipe<?>> recipes = getRecipes();
		if (recipes.isEmpty())
			return;
		if (recipeIndex >= recipes.size())
			recipeIndex = 0;

		IRecipe<?> recipe = recipes.get(recipeIndex);

		int rolls = inventory.getStackInSlot(0)
			.getCount();
		inventory.clear();

		List<ItemStack> list = new ArrayList<>();
		for (int roll = 0; roll < rolls; roll++) {
			List<ItemStack> results = new LinkedList<ItemStack>();
			if (recipe instanceof CuttingRecipe)
				results = ((CuttingRecipe) recipe).rollResults();
			else if (recipe instanceof StonecuttingRecipe || recipe.getType() == woodcuttingRecipeType.get())
				results.add(recipe.getResultItem()
					.copy());

			for (int i = 0; i < results.size(); i++) {
				ItemStack stack = results.get(i);
				ItemHelper.addToList(stack, list);
			}
		}
		for (int slot = 0; slot < list.size() && slot + 1 < inventory.getSlots(); slot++) {
			inventory.setStackInSlot(slot + 1, list.get(slot));
		}

	}

	private List<? extends IRecipe<?>> getRecipes() {
		Optional<CuttingRecipe> assemblyRecipe = SequencedAssemblyRecipe.getRecipe(level, inventory.getStackInSlot(0),
			AllRecipeTypes.CUTTING.getType(), CuttingRecipe.class);
		if (assemblyRecipe.isPresent() && filtering.test(assemblyRecipe.get()
			.getResultItem()))
			return ImmutableList.of(assemblyRecipe.get());

		Predicate<IRecipe<?>> types = RecipeConditions.isOfType(AllRecipeTypes.CUTTING.getType(),
			AllConfigs.SERVER.recipes.allowStonecuttingOnSaw.get() ? IRecipeType.STONECUTTING : null,
			AllConfigs.SERVER.recipes.allowWoodcuttingOnSaw.get() ? woodcuttingRecipeType.get() : null);

		List<IRecipe<?>> startedSearch = RecipeFinder.get(cuttingRecipesKey, level, types);
		return startedSearch.stream()
			.filter(RecipeConditions.outputMatchesFilter(filtering))
			.filter(RecipeConditions.firstIngredientMatches(inventory.getStackInSlot(0)))
			.filter(r -> !AllRecipeTypes.isManualRecipe(r))
			.collect(Collectors.toList());
	}

	public void insertItem(ItemEntity entity) {
		if (!canProcess())
			return;
		if (!inventory.isEmpty())
			return;
		if (!entity.isAlive())
			return;
		if (level.isClientSide)
			return;

		inventory.clear();
		ItemStack remainder = inventory.insertItem(0, entity.getItem()
			.copy(), false);
		if (remainder.isEmpty())
			entity.remove();
		else
			entity.setItem(remainder);
	}

	public void start(ItemStack inserted) {
		if (!canProcess())
			return;
		if (inventory.isEmpty())
			return;
		if (level.isClientSide && !isVirtual())
			return;

		List<? extends IRecipe<?>> recipes = getRecipes();
		boolean valid = !recipes.isEmpty();
		int time = 50;

		if (recipes.isEmpty()) {
			inventory.remainingTime = inventory.recipeDuration = 10;
			inventory.appliedRecipe = false;
			sendData();
			return;
		}

		if (valid) {
			recipeIndex++;
			if (recipeIndex >= recipes.size())
				recipeIndex = 0;
		}

		IRecipe<?> recipe = recipes.get(recipeIndex);
		if (recipe instanceof CuttingRecipe) {
			time = ((CuttingRecipe) recipe).getProcessingDuration();
		}

		inventory.remainingTime = time * Math.max(1, (inserted.getCount() / 5));
		inventory.recipeDuration = inventory.remainingTime;
		inventory.appliedRecipe = false;
		sendData();
	}

	protected boolean canProcess() {
		return getBlockState().getValue(SawBlock.FACING) == Direction.UP;
	}

	// Block Breaker

	@Override
	protected boolean shouldRun() {
		return getBlockState().getValue(SawBlock.FACING)
			.getAxis()
			.isHorizontal();
	}

	@Override
	protected BlockPos getBreakingPos() {
		return getBlockPos().relative(getBlockState().getValue(SawBlock.FACING));
	}

	@Override
	public void onBlockBroken(BlockState stateToBreak) {
		Optional<AbstractBlockBreakQueue> dynamicTree = TreeCutter.findDynamicTree(stateToBreak.getBlock(), breakingPos);
		if (dynamicTree.isPresent()) {
			dynamicTree.get().destroyBlocks(level, null, this::dropItemFromCutTree);
			return;
		}

		super.onBlockBroken(stateToBreak);
		TreeCutter.findTree(level, breakingPos)
			.destroyBlocks(level, null, this::dropItemFromCutTree);
	}

	public void dropItemFromCutTree(BlockPos pos, ItemStack stack) {
		float distance = (float) Math.sqrt(pos.distSqr(breakingPos));
		Vector3d dropPos = VecHelper.getCenterOf(pos);
		ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
		entity.setDeltaMovement(Vector3d.atLowerCornerOf(breakingPos.subtract(this.worldPosition))
			.scale(distance / 20f));
		level.addFreshEntity(entity);
	}

	@Override
	public boolean canBreak(BlockState stateToBreak, float blockHardness) {
		boolean sawable = isSawable(stateToBreak);
		return super.canBreak(stateToBreak, blockHardness) && sawable;
	}

	public static boolean isSawable(BlockState stateToBreak) {
		if (stateToBreak.is(BlockTags.LOGS) || AllTags.AllBlockTags.SLIMY_LOGS.matches(stateToBreak)
			|| stateToBreak.is(BlockTags.LEAVES))
			return true;
		Block block = stateToBreak.getBlock();
		if (block instanceof BambooBlock)
			return true;
		if (block instanceof StemGrownBlock)
			return true;
		if (block instanceof CactusBlock)
			return true;
		if (block instanceof SugarCaneBlock)
			return true;
		if (block instanceof KelpBlock)
			return true;
		if (block instanceof KelpTopBlock)
			return true;
		if (block instanceof ChorusPlantBlock)
			return true;
		if (TreeCutter.canDynamicTreeCutFrom(block))
			return true;
		return false;
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

}
