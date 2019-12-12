package com.simibubi.create.modules.contraptions.receivers;

import static com.simibubi.create.modules.contraptions.receivers.SawBlock.RUNNING;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.recipe.RecipeConditions;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.block.IHaveFilter;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SawTileEntity extends KineticTileEntity implements IHaveFilter {

	private static final Object cuttingRecipesKey = new Object();
	public ProcessingInventory inventory;
	private int recipeIndex;
	private ItemStack filter;
	private LazyOptional<IItemHandler> invProvider = LazyOptional.empty();

	public SawTileEntity() {
		super(AllTileEntities.SAW.type);
		inventory = new ProcessingInventory();
		inventory.remainingTime = -1;
		filter = ItemStack.EMPTY;
		recipeIndex = 0;
		invProvider = LazyOptional.of(() -> inventory);
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	@Override
	public void onSpeedChanged() {
		boolean shouldRun = Math.abs(getSpeed()) > 1 / 64f;
		boolean running = getBlockState().get(RUNNING);
		if (shouldRun != running)
			world.setBlockState(pos, getBlockState().with(RUNNING, shouldRun), 2 | 16);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		inventory.write(compound);
		compound.put("Filter", filter.write(new CompoundNBT()));
		compound.putInt("RecipeIndex", recipeIndex);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		inventory = ProcessingInventory.read(compound);
		recipeIndex = compound.getInt("RecipeIndex");
		filter = ItemStack.read(compound.getCompound("Filter"));
	}

	@Override
	public void tick() {
		super.tick();
		if (!canProcess())
			return;
		if (getSpeed() == 0)
			return;
		if (inventory.remainingTime == -1) {
			if (!inventory.isEmpty() && !inventory.appliedRecipe)
				start();
			return;
		}

		float processingSpeed = MathHelper.clamp(Math.abs(getSpeed()) / 32, 1, 128);
		inventory.remainingTime -= processingSpeed;

		if (inventory.remainingTime > 0)
			spawnParticles(inventory.getStackInSlot(0));

		if (world.isRemote)
			return;

		if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
			applyRecipe();
			inventory.appliedRecipe = true;
			sendData();
			return;
		}

		Vec3d itemMovement = getItemMovementVec();
		Direction itemMovementFacing = Direction.getFacingFromVector(itemMovement.x, itemMovement.y, itemMovement.z);
		Vec3d outPos = VecHelper.getCenterOf(pos).add(itemMovement.scale(.5f).add(0, .5, 0));
		Vec3d outMotion = itemMovement.scale(.0625).add(0, .125, 0);

		if (inventory.remainingTime <= 0) {

			// Try moving items onto the belt
			BlockPos nextPos = pos.add(itemMovement.x, itemMovement.y, itemMovement.z);
			if (AllBlocks.BELT.typeOf(world.getBlockState(nextPos))) {
				TileEntity te = world.getTileEntity(nextPos);
				if (te != null && te instanceof BeltTileEntity) {
					for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
						ItemStack stack = inventory.getStackInSlot(slot);
						if (stack.isEmpty())
							continue;

						if (itemMovementFacing.getAxis() == Axis.Z)
							itemMovementFacing = itemMovementFacing.getOpposite();
						if (((BeltTileEntity) te).tryInsertingFromSide(itemMovementFacing, stack, false))
							inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
						else {
							inventory.remainingTime = 0;
							return;
						}
					}
					inventory.clear();
					inventory.remainingTime = -1;
					sendData();
				}
			}

			// Try moving items onto next saw
			if (AllBlocks.SAW.typeOf(world.getBlockState(nextPos))) {
				TileEntity te = world.getTileEntity(nextPos);
				if (te != null && te instanceof SawTileEntity) {
					SawTileEntity sawTileEntity = (SawTileEntity) te;
					Vec3d otherMovement = sawTileEntity.getItemMovementVec();
					if (Direction.getFacingFromVector(otherMovement.x, otherMovement.y,
							otherMovement.z) != itemMovementFacing.getOpposite()) {
						for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
							ItemStack stack = inventory.getStackInSlot(slot);
							if (stack.isEmpty())
								continue;

							ProcessingInventory sawInv = sawTileEntity.inventory;
							if (sawInv.isEmpty()) {
								sawInv.insertItem(0, stack, false);
								inventory.setInventorySlotContents(slot, ItemStack.EMPTY);

							} else {
								inventory.remainingTime = 0;
								return;
							}
						}
						inventory.clear();
						inventory.remainingTime = -1;
						sendData();
					}
				}
			}

			// Eject Items
			for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
				ItemStack stack = inventory.getStackInSlot(slot);
				if (stack.isEmpty())
					continue;
				ItemEntity entityIn = new ItemEntity(world, outPos.x, outPos.y, outPos.z, stack);
				entityIn.setMotion(outMotion);
				world.addEntity(entityIn);
			}
			inventory.clear();
			world.updateComparatorOutputLevel(pos, getBlockState().getBlock());
			inventory.remainingTime = -1;
			sendData();
			return;
		}

		return;
	}

	@Override
	public void remove() {
		super.remove();
		invProvider.invalidate();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return invProvider.cast();
		return super.getCapability(cap, side);
	}

	protected void spawnParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		IParticleData particleData = null;
		float speed = 1;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockParticleData(ParticleTypes.BLOCK,
					((BlockItem) stack.getItem()).getBlock().getDefaultState());
		else {
			particleData = new ItemParticleData(ParticleTypes.ITEM, stack);
			speed = .125f;
		}

		Random r = world.rand;
		Vec3d vec = getItemMovementVec();
		Vec3d pos = VecHelper.getCenterOf(this.pos);
		float offset = inventory.recipeDuration != 0 ? (float) (inventory.remainingTime) / inventory.recipeDuration : 0;
		offset -= .5f;
		world.addParticle(particleData, pos.getX() + -vec.x * offset, pos.getY() + .45f, pos.getZ() + -vec.z * offset,
				vec.x * speed, r.nextFloat() * speed, vec.z * speed);
	}

	public Vec3d getItemMovementVec() {
		boolean alongX = !getBlockState().get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
		int offset = getSpeed() < 0 ? -1 : 1;
		return new Vec3d(offset * (alongX ? 1 : 0), 0, offset * (alongX ? 0 : -1));
	}

	private void applyRecipe() {
		List<? extends IRecipe<?>> recipes = getRecipes();
		if (recipes.isEmpty())
			return;
		if (recipeIndex >= recipes.size())
			recipeIndex = 0;

		IRecipe<?> recipe = recipes.get(recipeIndex);

		int rolls = inventory.getStackInSlot(0).getCount();
		inventory.clear();

		for (int roll = 0; roll < rolls; roll++) {
			List<ItemStack> results = new LinkedList<ItemStack>();
			if (recipe instanceof CuttingRecipe)
				results = ((CuttingRecipe) recipe).rollResults();
			else if (recipe instanceof StonecuttingRecipe)
				results.add(recipe.getRecipeOutput().copy());

			for (int i = 0; i < results.size(); i++) {
				ItemStack stack = results.get(i);

				for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
					stack = inventory.getItems().insertItem(slot, stack, false);

					if (stack.isEmpty())
						break;
				}
			}
		}

	}

	private List<? extends IRecipe<?>> getRecipes() {
		return RecipeFinder
				.get(cuttingRecipesKey, world,
						RecipeConditions.isOfType(IRecipeType.STONECUTTING, AllRecipes.Types.CUTTING))
				.search().filter(RecipeConditions.outputMatchesFilter(filter))
				.filter(RecipeConditions.firstIngredientMatches(inventory.getStackInSlot(0))).asList();
	}

	public void insertItem(ItemEntity entity) {
		if (!canProcess())
			return;
		if (!inventory.isEmpty())
			return;
		if (world.isRemote)
			return;

		inventory.clear();
		inventory.setInventorySlotContents(0, entity.getItem().copy());
		entity.remove();
		start();
	}

	public void start() {
		if (!canProcess())
			return;
		if (inventory.isEmpty())
			return;
		if (world.isRemote)
			return;

		List<? extends IRecipe<?>> recipes = getRecipes();
		boolean valid = !recipes.isEmpty();
		int time = 100;

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

		inventory.remainingTime = time * Math.max(1, (inventory.getStackInSlot(0).getCount() / 5));
		inventory.recipeDuration = inventory.remainingTime;
		inventory.appliedRecipe = false;
		sendData();
	}

	protected boolean canProcess() {
		return getBlockState().get(SawBlock.FACING) == Direction.UP;
	}

	@Override
	public void setFilter(ItemStack stack) {
		filter = stack.copy();
		markDirty();
		sendData();
	}

	@Override
	public ItemStack getFilter() {
		return filter;
	}

}
