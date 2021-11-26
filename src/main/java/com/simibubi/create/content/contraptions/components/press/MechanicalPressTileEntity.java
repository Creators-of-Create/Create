package com.simibubi.create.content.contraptions.components.press;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.advancement.ITriggerable;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MechanicalPressTileEntity extends BasinOperatingTileEntity {

	private static final Object compressingRecipesKey = new Object();
	public List<ItemStack> pressedItems = new ArrayList<>();
	public BeltProcessingBehaviour processingBehaviour;

	public int prevRunningTicks;
	public int runningTicks;
	static final int CYCLE = 240;
	static final int ENTITY_SCAN = 10;
	int entityScanCooldown;

	public boolean running;
	public Mode mode;
	public boolean finished;

	public MechanicalPressTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		mode = Mode.WORLD;
		entityScanCooldown = ENTITY_SCAN;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		processingBehaviour =
			new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> BeltPressingCallbacks.onItemReceived(s, i, this))
				.whileItemHeld((s, i) -> BeltPressingCallbacks.whenItemHeld(s, i, this));
		behaviours.add(processingBehaviour);
	}

	@Override
	protected void fromTag(CompoundTag compound, boolean clientPacket) {
		running = compound.getBoolean("Running");
		mode = Mode.values()[compound.getInt("Mode")];
		finished = compound.getBoolean("Finished");
		prevRunningTicks = runningTicks = compound.getInt("Ticks");
		super.fromTag(compound, clientPacket);

		if (clientPacket) {
			NBTHelper.iterateCompoundList(compound.getList("ParticleItems", NBT.TAG_COMPOUND),
				c -> pressedItems.add(ItemStack.of(c)));
			spawnParticles();
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putInt("Mode", mode.ordinal());
		compound.putBoolean("Finished", finished);
		compound.putInt("Ticks", runningTicks);
		super.write(compound, clientPacket);

		if (clientPacket) {
			compound.put("ParticleItems", NBTHelper.writeCompoundList(pressedItems, ItemStack::serializeNBT));
			pressedItems.clear();
		}
	}

	@Override
	public AABB makeRenderBoundingBox() {
		return new AABB(worldPosition).expandTowards(0, -1.5, 0)
			.expandTowards(0, 1, 0);
	}

	public float getRenderedHeadOffset(float partialTicks) {
		if (!running)
			return 0;
		int runningTicks = Math.abs(this.runningTicks);
		float ticks = Mth.lerp(partialTicks, prevRunningTicks, runningTicks);
		if (runningTicks < (CYCLE * 2) / 3)
			return (float) Mth.clamp(Math.pow(ticks / CYCLE * 2, 3), 0, 1) * mode.headOffset;
		return Mth.clamp((CYCLE - ticks) / CYCLE * 3, 0, 1) * mode.headOffset;
	}

	public void start(Mode mode) {
		this.mode = mode;
		running = true;
		prevRunningTicks = 0;
		runningTicks = 0;
		pressedItems.clear();
		sendData();
	}

	public boolean inWorld() {
		return mode == Mode.WORLD;
	}

	public boolean onBasin() {
		return mode == Mode.BASIN;
	}

	@Override
	public void tick() {
		super.tick();

		if (!running || level == null) {
			if (hasLevel() && !level.isClientSide) {

				if (getSpeed() == 0)
					return;
				if (entityScanCooldown > 0)
					entityScanCooldown--;
				if (entityScanCooldown <= 0) {
					entityScanCooldown = ENTITY_SCAN;
					if (TileEntityBehaviour.get(level, worldPosition.below(2), TransportedItemStackHandlerBehaviour.TYPE) != null)
						return;
					if (AllBlocks.BASIN.has(level.getBlockState(worldPosition.below(2))))
						return;

					for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class,
						new AABB(worldPosition.below()).deflate(.125f))) {
						if (!itemEntity.isAlive() || !itemEntity.isOnGround())
							continue;
						ItemStack stack = itemEntity.getItem();
						Optional<PressingRecipe> recipe = getRecipe(stack);
						if (!recipe.isPresent())
							continue;
						start(Mode.WORLD);
						return;
					}
				}

			}
			return;
		}

		if (level.isClientSide && runningTicks == -CYCLE / 2) {
			prevRunningTicks = CYCLE / 2;
			return;
		}

		if (runningTicks == CYCLE / 2 && getSpeed() != 0) {
			if (inWorld())
				applyPressingInWorld();
			if (onBasin())
				applyCompactingOnBasin();

			if (level.getBlockState(worldPosition.below(2)).getSoundType() == SoundType.WOOL)
				AllSoundEvents.MECHANICAL_PRESS_ACTIVATION_ON_BELT.playOnServer(level, worldPosition);
			else
				AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(level, worldPosition, .5f, .75f + (Math.abs(getSpeed()) / 1024f));

			if (!level.isClientSide)
				sendData();
		}

		if (!level.isClientSide && runningTicks > CYCLE) {
			finished = true;
			running = false;

			if (onBasin() && matchBasinRecipe(currentRecipe)
				&& getBasin().filter(BasinTileEntity::canContinueProcessing)
					.isPresent())
				startProcessingBasin();
			else
				basinChecker.scheduleUpdate();

			pressedItems.clear();
			sendData();
			return;
		}

		prevRunningTicks = runningTicks;
		runningTicks += getRunningTickSpeed();
		if (prevRunningTicks < CYCLE / 2 && runningTicks >= CYCLE / 2) {
			runningTicks = CYCLE / 2;
			// Pause the ticks until a packet is received
			if (level.isClientSide && !isVirtual())
				runningTicks = -(CYCLE / 2);
		}
	}

	protected void applyCompactingOnBasin() {
		if (level.isClientSide)
			return;
		pressedItems.clear();
		applyBasinRecipe();
		Optional<BasinTileEntity> basin = getBasin();
		if (basin.isPresent()) {
			SmartInventory inputs = basin.get()
				.getInputInventory();
			for (int slot = 0; slot < inputs.getSlots(); slot++) {
				ItemStack stackInSlot = inputs.getItem(slot);
				if (stackInSlot.isEmpty())
					continue;
				pressedItems.add(stackInSlot);
			}
		}
		sendData();
	}

	protected void applyPressingInWorld() {
		AABB bb = new AABB(worldPosition.below(1));
		boolean bulk = canProcessInBulk();
		pressedItems.clear();
		if (level.isClientSide)
			return;
		for (Entity entity : level.getEntities(null, bb)) {
			if (!(entity instanceof ItemEntity))
				continue;
			if (!entity.isAlive() || !entity.isOnGround())
				continue;
			ItemEntity itemEntity = (ItemEntity) entity;
			ItemStack item = itemEntity.getItem();
			pressedItems.add(item);
			sendData();
			Optional<PressingRecipe> recipe = getRecipe(item);
			if (!recipe.isPresent())
				continue;

			if (bulk || item.getCount() == 1) {
				InWorldProcessing.applyRecipeOn(itemEntity, recipe.get());
			} else {
				for (ItemStack result : InWorldProcessing.applyRecipeOn(ItemHandlerHelper.copyStackWithSize(item, 1),
					recipe.get())) {
					ItemEntity created =
							new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
					created.setDefaultPickUpDelay();
					created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, Create.RANDOM, .05f));
					level.addFreshEntity(created);
				}
				item.shrink(1);
			}

			AllTriggers.triggerForNearbyPlayers(AllTriggers.BONK, level, worldPosition, 4);
			entityScanCooldown = 0;

			if (!bulk)
				break;
		}
	}

	public static boolean canProcessInBulk() {
		return AllConfigs.SERVER.recipes.bulkPressing.get();
	}

	public int getRunningTickSpeed() {
		if (getSpeed() == 0)
			return 0;
		return (int) Mth.lerp(Mth.clamp(Math.abs(getSpeed()) / 512f, 0, 1), 1, 60);
	}

	protected void spawnParticles() {
		if (pressedItems.isEmpty())
			return;

		if (mode == Mode.BASIN)
			pressedItems.forEach(stack -> makeCompactingParticleEffect(VecHelper.getCenterOf(worldPosition.below(2)), stack));
		if (mode == Mode.BELT)
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(worldPosition.below(2))
				.add(0, 8 / 16f, 0), stack));
		if (mode == Mode.WORLD)
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(worldPosition.below(1))
				.add(0, -1 / 4f, 0), stack));

		pressedItems.clear();
	}

	public void makePressingParticleEffect(Vec3 pos, ItemStack stack) {
		if (level == null || !level.isClientSide)
			return;
		for (int i = 0; i < 20; i++) {
			Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .125f)
				.multiply(1, 0, 1);
			level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), pos.x, pos.y - .25f, pos.z, motion.x,
				motion.y + .125f, motion.z);
		}
	}

	public void makeCompactingParticleEffect(Vec3 pos, ItemStack stack) {
		if (level == null || !level.isClientSide)
			return;
		for (int i = 0; i < 20; i++) {
			Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .175f)
				.multiply(1, 0, 1);
			level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z, motion.x,
				motion.y + .25f, motion.z);
		}
	}

	private static final RecipeWrapper pressingInv = new RecipeWrapper(new ItemStackHandler(1));

	public Optional<PressingRecipe> getRecipe(ItemStack item) {
		Optional<PressingRecipe> assemblyRecipe =
			SequencedAssemblyRecipe.getRecipe(level, item, AllRecipeTypes.PRESSING.getType(), PressingRecipe.class);
		if (assemblyRecipe.isPresent())
			return assemblyRecipe;

		pressingInv.setItem(0, item);
		return AllRecipeTypes.PRESSING.find(pressingInv, level);
	}

	private static final List<ResourceLocation> RECIPE_DENY_LIST =
		ImmutableList.of(new ResourceLocation("occultism", "spirit_trade"), new ResourceLocation("occultism", "ritual"));

	public static <C extends Container> boolean canCompress(Recipe<C> recipe) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if (!(recipe instanceof CraftingRecipe))
			return false;
		
		RecipeSerializer<?> serializer = recipe.getSerializer();
		for (ResourceLocation denied : RECIPE_DENY_LIST) 
			if (serializer != null && denied.equals(serializer.getRegistryName()))
				return false;
		
		return AllConfigs.SERVER.recipes.allowShapedSquareInPress.get()
			&& (ingredients.size() == 4 || ingredients.size() == 9) && ItemHelper.condenseIngredients(ingredients)
				.size() == 1;
	}

	@Override
	protected <C extends Container> boolean matchStaticFilters(Recipe<C> recipe) {
		return (recipe instanceof CraftingRecipe && !(recipe instanceof MechanicalCraftingRecipe)
			&& canCompress(recipe) && !AllRecipeTypes.isManualRecipe(recipe))
			|| recipe.getType() == AllRecipeTypes.COMPACTING.getType();
	}

	@Override
	protected Object getRecipeCacheKey() {
		return compressingRecipesKey;
	}

	@Override
	public void startProcessingBasin() {
		if (running && runningTicks <= CYCLE / 2)
			return;
		super.startProcessingBasin();
		start(Mode.BASIN);
	}

	@Override
	protected void onBasinRemoved() {
		pressedItems.clear();
		running = false;
		runningTicks = 0;
		sendData();
	}

	@Override
	protected boolean isRunning() {
		return running;
	}

	@Override
	protected Optional<ITriggerable> getProcessedRecipeTrigger() {
		return Optional.of(AllTriggers.PRESS_COMPACT);
	}

	public enum Mode {
		WORLD(1), BELT(19f / 16f), BASIN(22f / 16f)

		;

		float headOffset;

		Mode(float headOffset) {
			this.headOffset = headOffset;
		}
	}

}
