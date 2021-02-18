package com.simibubi.create.content.contraptions.components.press;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.logistics.InWorldProcessing;
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

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants.NBT;
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

	public MechanicalPressTileEntity(TileEntityType<? extends MechanicalPressTileEntity> type) {
		super(type);
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
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		running = compound.getBoolean("Running");
		mode = Mode.values()[compound.getInt("Mode")];
		finished = compound.getBoolean("Finished");
		prevRunningTicks = runningTicks = compound.getInt("Ticks");
		super.fromTag(state, compound, clientPacket);

		if (clientPacket) {
			NBTHelper.iterateCompoundList(compound.getList("ParticleItems", NBT.TAG_COMPOUND),
				c -> pressedItems.add(ItemStack.read(c)));
			spawnParticles();
		}
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
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
	public AxisAlignedBB makeRenderBoundingBox() {
		return new AxisAlignedBB(pos).expand(0, -1.5, 0)
			.expand(0, 1, 0);
	}

	public float getRenderedHeadOffset(float partialTicks) {
		if (!running)
			return 0;
		int runningTicks = Math.abs(this.runningTicks);
		float ticks = MathHelper.lerp(partialTicks, prevRunningTicks, runningTicks);
		if (runningTicks < (CYCLE * 2) / 3)
			return (float) MathHelper.clamp(Math.pow(ticks / CYCLE * 2, 3), 0, 1) * mode.headOffset;
		return MathHelper.clamp((CYCLE - ticks) / CYCLE * 3, 0, 1) * mode.headOffset;
	}

	public void start(Mode mode) {
		this.mode = mode;
		running = true;
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

		if (!running || world == null) {
			if (hasWorld() && !world.isRemote) {

				if (getSpeed() == 0)
					return;
				if (entityScanCooldown > 0)
					entityScanCooldown--;
				if (entityScanCooldown <= 0) {
					entityScanCooldown = ENTITY_SCAN;
					if (TileEntityBehaviour.get(world, pos.down(2), TransportedItemStackHandlerBehaviour.TYPE) != null)
						return;
					if (AllBlocks.BASIN.has(world.getBlockState(pos.down(2))))
						return;

					for (ItemEntity itemEntity : world.getEntitiesWithinAABB(ItemEntity.class,
						new AxisAlignedBB(pos.down()).shrink(.125f))) {
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

		if (world.isRemote && runningTicks == -CYCLE / 2) {
			prevRunningTicks = CYCLE / 2;
			return;
		}

		if (runningTicks == CYCLE / 2 && getSpeed() != 0) {
			if (inWorld())
				applyPressingInWorld();
			if (onBasin())
				applyCompactingOnBasin();
			if (!world.isRemote) {
				world.playSound(null, getPos(), AllSoundEvents.MECHANICAL_PRESS_ITEM_BREAK.get(), SoundCategory.BLOCKS,
					.5f, 1f);
				world.playSound(null, getPos(), AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.get(), SoundCategory.BLOCKS,
					.125f, 1f);
			}
		}

		if (!world.isRemote && runningTicks > CYCLE) {
			finished = true;
			running = false;

			if (onBasin() && matchBasinRecipe(currentRecipe))
				startProcessingBasin();

			pressedItems.clear();
			sendData();
			return;
		}

		prevRunningTicks = runningTicks;
		runningTicks += getRunningTickSpeed();
		if (prevRunningTicks < CYCLE / 2 && runningTicks >= CYCLE / 2) {
			runningTicks = CYCLE / 2;
			// Pause the ticks until a packet is received
			if (world.isRemote)
				runningTicks = -(CYCLE / 2);
		}
	}

	protected void applyCompactingOnBasin() {
		if (world.isRemote)
			return;
		pressedItems.clear();
		applyBasinRecipe();
		Optional<BasinTileEntity> basin = getBasin();
		SmartInventory inputs = basin.get()
			.getInputInventory();
		if (basin.isPresent()) {
			for (int slot = 0; slot < inputs.getSlots(); slot++) {
				ItemStack stackInSlot = inputs.getStackInSlot(slot);
				if (stackInSlot.isEmpty())
					continue;
				pressedItems.add(stackInSlot);
			}
		}
		sendData();
	}

	protected void applyPressingInWorld() {
		AxisAlignedBB bb = new AxisAlignedBB(pos.down(1));
		pressedItems.clear();
		if (world.isRemote)
			return;
		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, bb)) {
			if (!(entity instanceof ItemEntity))
				continue;
			if (!entity.isAlive() || !entity.isOnGround())
				continue;
			ItemEntity itemEntity = (ItemEntity) entity;
			pressedItems.add(itemEntity.getItem());
			sendData();
			Optional<PressingRecipe> recipe = getRecipe(itemEntity.getItem());
			if (!recipe.isPresent())
				continue;
			InWorldProcessing.applyRecipeOn(itemEntity, recipe.get());
			AllTriggers.triggerForNearbyPlayers(AllTriggers.BONK, world, pos, 4);
		}
	}

	public int getRunningTickSpeed() {
		if (getSpeed() == 0)
			return 0;
		return (int) MathHelper.lerp(MathHelper.clamp(Math.abs(getSpeed()) / 512f, 0, 1), 1, 60);
	}

	protected void spawnParticles() {
		if (pressedItems.isEmpty())
			return;

		if (mode == Mode.BASIN)
			pressedItems.forEach(stack -> makeCompactingParticleEffect(VecHelper.getCenterOf(pos.down(2)), stack));
		if (mode == Mode.BELT)
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(pos.down(2))
				.add(0, 8 / 16f, 0), stack));
		if (mode == Mode.WORLD)
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(pos.down(1))
				.add(0, -1 / 4f, 0), stack));

		pressedItems.clear();
	}

	public void makePressingParticleEffect(Vector3d pos, ItemStack stack) {
		if (world == null || !world.isRemote)
			return;
		for (int i = 0; i < 20; i++) {
			Vector3d motion = VecHelper.offsetRandomly(Vector3d.ZERO, world.rand, .125f)
				.mul(1, 0, 1);
			world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), pos.x, pos.y - .25f, pos.z, motion.x,
				motion.y + .125f, motion.z);
		}
	}

	public void makeCompactingParticleEffect(Vector3d pos, ItemStack stack) {
		if (world == null || !world.isRemote)
			return;
		for (int i = 0; i < 20; i++) {
			Vector3d motion = VecHelper.offsetRandomly(Vector3d.ZERO, world.rand, .175f)
				.mul(1, 0, 1);
			world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z, motion.x,
				motion.y + .25f, motion.z);
		}
	}

	private static final RecipeWrapper pressingInv = new RecipeWrapper(new ItemStackHandler(1));

	public Optional<PressingRecipe> getRecipe(ItemStack item) {
		pressingInv.setInventorySlotContents(0, item);
		return AllRecipeTypes.PRESSING.find(pressingInv, world);
	}

	public static boolean canCompress(NonNullList<Ingredient> ingredients) {
		return AllConfigs.SERVER.recipes.allowShapedSquareInPress.get()
			&& (ingredients.size() == 4 || ingredients.size() == 9) && ItemHelper.condenseIngredients(ingredients)
				.size() == 1;
	}

	@Override
	protected <C extends IInventory> boolean matchStaticFilters(IRecipe<C> recipe) {
		return (recipe instanceof ICraftingRecipe && canCompress(recipe.getIngredients()))
			|| recipe.getType() == AllRecipeTypes.COMPACTING.type;
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

	enum Mode {
		WORLD(1), BELT(19f / 16f), BASIN(22f / 16f)

		;

		float headOffset;

		Mode(float headOffset) {
			this.headOffset = headOffset;
		}
	}

}
