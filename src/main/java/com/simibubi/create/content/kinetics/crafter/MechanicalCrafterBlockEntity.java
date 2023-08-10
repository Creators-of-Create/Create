package com.simibubi.create.content.kinetics.crafter;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.create.content.kinetics.crafter.RecipeGridHandler.GroupedItems;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;

import net.createmod.catnip.utility.BlockFace;
import net.createmod.catnip.utility.Pointing;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class MechanicalCrafterBlockEntity extends KineticBlockEntity {

	enum Phase {
		IDLE, ACCEPTING, ASSEMBLING, EXPORTING, WAITING, CRAFTING, INSERTING;
	}

	public static class Inventory extends SmartInventory {

		private MechanicalCrafterBlockEntity blockEntity;

		public Inventory(MechanicalCrafterBlockEntity blockEntity) {
			super(1, blockEntity, 1, false);
			this.blockEntity = blockEntity;
			forbidExtraction();
			whenContentsChanged(slot -> {
				if (getItem(slot).isEmpty())
					return;
				if (blockEntity.phase == Phase.IDLE)
					blockEntity.checkCompletedRecipe(false);
			});
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (blockEntity.phase != Phase.IDLE)
				return stack;
			if (blockEntity.covered)
				return stack;
			ItemStack insertItem = super.insertItem(slot, stack, simulate);
			if (insertItem.getCount() != stack.getCount() && !simulate)
				blockEntity.getLevel()
					.playSound(null, blockEntity.getBlockPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .5f);
			return insertItem;
		}

	}

	protected Inventory inventory;
	protected GroupedItems groupedItems = new GroupedItems();
	protected ConnectedInput input = new ConnectedInput();
	protected LazyOptional<IItemHandler> invSupplier =
		LazyOptional.of(() -> input.getItemHandler(level, worldPosition));
	protected boolean reRender;
	protected Phase phase;
	protected int countDown;
	protected boolean covered;
	protected boolean wasPoweredBefore;

	protected GroupedItems groupedItemsBeforeCraft; // for rendering on client
	private InvManipulationBehaviour inserting;
	private EdgeInteractionBehaviour connectivity;

	private ItemStack scriptedResult = ItemStack.EMPTY;

	public MechanicalCrafterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(20);
		phase = Phase.IDLE;
		groupedItemsBeforeCraft = new GroupedItems();
		inventory = new Inventory(this);

		// Does not get serialized due to active checking in tick
		wasPoweredBefore = true;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		inserting = new InvManipulationBehaviour(this, this::getTargetFace);
		connectivity = new EdgeInteractionBehaviour(this, ConnectedInputHandler::toggleConnection)
			.connectivity(ConnectedInputHandler::shouldConnect)
			.require(AllItems.WRENCH.get());
		behaviours.add(inserting);
		behaviours.add(connectivity);
		registerAwardables(behaviours, AllAdvancements.CRAFTER, AllAdvancements.CRAFTER_LAZY);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		if (!Mth.equal(getSpeed(), 0)) {
			award(AllAdvancements.CRAFTER);
			if (Math.abs(getSpeed()) < 5)
				award(AllAdvancements.CRAFTER_LAZY);
		}
	}

	public void blockChanged() {
		removeBehaviour(InvManipulationBehaviour.TYPE);
		inserting = new InvManipulationBehaviour(this, this::getTargetFace);
		attachBehaviourLate(inserting);
	}

	public BlockFace getTargetFace(Level world, BlockPos pos, BlockState state) {
		return new BlockFace(pos, MechanicalCrafterBlock.getTargetDirection(state));
	}

	public Direction getTargetDirection() {
		return MechanicalCrafterBlock.getTargetDirection(getBlockState());
	}

	@Override
	public void writeSafe(CompoundTag compound) {
		super.writeSafe(compound);
		if (input == null)
			return;

		CompoundTag inputNBT = new CompoundTag();
		input.write(inputNBT);
		compound.put("ConnectedInput", inputNBT);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Inventory", inventory.serializeNBT());

		CompoundTag inputNBT = new CompoundTag();
		input.write(inputNBT);
		compound.put("ConnectedInput", inputNBT);

		CompoundTag groupedItemsNBT = new CompoundTag();
		groupedItems.write(groupedItemsNBT);
		compound.put("GroupedItems", groupedItemsNBT);

		compound.putString("Phase", phase.name());
		compound.putInt("CountDown", countDown);
		compound.putBoolean("Cover", covered);

		super.write(compound, clientPacket);

		if (clientPacket && reRender) {
			compound.putBoolean("Redraw", true);
			reRender = false;
		}
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		Phase phaseBefore = phase;
		GroupedItems before = this.groupedItems;

		inventory.deserializeNBT(compound.getCompound("Inventory"));
		input.read(compound.getCompound("ConnectedInput"));
		groupedItems = GroupedItems.read(compound.getCompound("GroupedItems"));
		phase = Phase.IDLE;
		String name = compound.getString("Phase");
		for (Phase phase : Phase.values())
			if (phase.name()
				.equals(name))
				this.phase = phase;
		countDown = compound.getInt("CountDown");
		covered = compound.getBoolean("Cover");
		super.read(compound, clientPacket);
		if (!clientPacket)
			return;
		if (compound.contains("Redraw"))
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
		if (phaseBefore != phase && phase == Phase.CRAFTING)
			groupedItemsBeforeCraft = before;
		if (phaseBefore == Phase.EXPORTING && phase == Phase.WAITING) {
			Direction facing = getBlockState().getValue(MechanicalCrafterBlock.HORIZONTAL_FACING);
			Vec3 vec = Vec3.atLowerCornerOf(facing.getNormal())
				.scale(.75)
				.add(VecHelper.getCenterOf(worldPosition));
			Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(getBlockState());
			vec = vec.add(Vec3.atLowerCornerOf(targetDirection.getNormal())
				.scale(1));
			level.addParticle(ParticleTypes.CRIT, vec.x, vec.y, vec.z, 0, 0, 0);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		invSupplier.invalidate();
	}

	public int getCountDownSpeed() {
		if (getSpeed() == 0)
			return 0;
		return Mth.clamp((int) Math.abs(getSpeed()), 4, 250);
	}

	@Override
	public void tick() {
		super.tick();

		if (phase == Phase.ACCEPTING)
			return;

		boolean onClient = level.isClientSide;
		boolean runLogic = !onClient || isVirtual();

		if (wasPoweredBefore != level.hasNeighborSignal(worldPosition)) {
			wasPoweredBefore = level.hasNeighborSignal(worldPosition);
			if (wasPoweredBefore) {
				if (!runLogic)
					return;
				checkCompletedRecipe(true);
			}
		}

		if (phase == Phase.ASSEMBLING) {
			countDown -= getCountDownSpeed();
			if (countDown < 0) {
				countDown = 0;
				if (!runLogic)
					return;
				if (RecipeGridHandler.getTargetingCrafter(this) != null) {
					phase = Phase.EXPORTING;
					countDown = 1000;
					sendData();
					return;
				}

				ItemStack result =
					isVirtual() ? scriptedResult : RecipeGridHandler.tryToApplyRecipe(level, groupedItems);

				if (result != null) {
					List<ItemStack> containers = new ArrayList<>();
					groupedItems.grid.values()
						.forEach(stack -> {
							if (stack.hasContainerItem())
								containers.add(stack.getContainerItem()
									.copy());
						});

					if (isVirtual())
						groupedItemsBeforeCraft = groupedItems;

					groupedItems = new GroupedItems(result);
					for (int i = 0; i < containers.size(); i++) {
						ItemStack stack = containers.get(i);
						GroupedItems container = new GroupedItems();
						container.grid.put(Pair.of(i, 0), stack);
						container.mergeOnto(groupedItems, Pointing.LEFT);
					}

					phase = Phase.CRAFTING;
					countDown = 2000;
					sendData();
					return;
				}
				ejectWholeGrid();
				return;
			}
		}

		if (phase == Phase.EXPORTING) {
			countDown -= getCountDownSpeed();

			if (countDown < 0) {
				countDown = 0;
				if (!runLogic)
					return;

				MechanicalCrafterBlockEntity targetingCrafter = RecipeGridHandler.getTargetingCrafter(this);
				if (targetingCrafter == null) {
					ejectWholeGrid();
					return;
				}

				Pointing pointing = getBlockState().getValue(MechanicalCrafterBlock.POINTING);
				groupedItems.mergeOnto(targetingCrafter.groupedItems, pointing);
				groupedItems = new GroupedItems();

				float pitch = targetingCrafter.groupedItems.grid.size() * 1 / 16f + .5f;
				AllSoundEvents.CRAFTER_CLICK.playOnServer(level, worldPosition, 1, pitch);

				phase = Phase.WAITING;
				countDown = 0;
				sendData();
				targetingCrafter.continueIfAllPrecedingFinished();
				targetingCrafter.sendData();
				return;
			}
		}

		if (phase == Phase.CRAFTING) {

			if (onClient) {
				Direction facing = getBlockState().getValue(MechanicalCrafterBlock.HORIZONTAL_FACING);
				float progress = countDown / 2000f;
				Vec3 facingVec = Vec3.atLowerCornerOf(facing.getNormal());
				Vec3 vec = facingVec.scale(.65)
					.add(VecHelper.getCenterOf(worldPosition));
				Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .125f)
					.multiply(VecHelper.axisAlingedPlaneOf(facingVec))
					.normalize()
					.scale(progress * .5f)
					.add(vec);
				if (progress > .5f)
					level.addParticle(ParticleTypes.CRIT, offset.x, offset.y, offset.z, 0, 0, 0);

				if (!groupedItemsBeforeCraft.grid.isEmpty() && progress < .5f) {
					if (groupedItems.grid.containsKey(Pair.of(0, 0))) {
						ItemStack stack = groupedItems.grid.get(Pair.of(0, 0));
						groupedItemsBeforeCraft = new GroupedItems();

						for (int i = 0; i < 10; i++) {
							Vec3 randVec = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .125f)
								.multiply(VecHelper.axisAlingedPlaneOf(facingVec))
								.normalize()
								.scale(.25f);
							Vec3 offset2 = randVec.add(vec);
							randVec = randVec.scale(.35f);
							level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), offset2.x, offset2.y,
								offset2.z, randVec.x, randVec.y, randVec.z);
						}
					}
				}
			}

			int prev = countDown;
			countDown -= getCountDownSpeed();

			if (countDown < 1000 && prev >= 1000) {
				AllSoundEvents.CRAFTER_CLICK.playOnServer(level, worldPosition, 1, 2);
				AllSoundEvents.CRAFTER_CRAFT.playOnServer(level, worldPosition);
			}

			if (countDown < 0) {
				countDown = 0;
				if (!runLogic)
					return;
				tryInsert();
				return;
			}
		}

		if (phase == Phase.INSERTING) {
			if (runLogic && isTargetingBelt())
				tryInsert();
			return;
		}
	}

	protected boolean isTargetingBelt() {
		DirectBeltInputBehaviour behaviour = getTargetingBelt();
		return behaviour != null && behaviour.canInsertFromSide(getTargetDirection());
	}

	protected DirectBeltInputBehaviour getTargetingBelt() {
		BlockPos targetPos = worldPosition.relative(getTargetDirection());
		return BlockEntityBehaviour.get(level, targetPos, DirectBeltInputBehaviour.TYPE);
	}

	public void tryInsert() {
		if (!inserting.hasInventory() && !isTargetingBelt()) {
			ejectWholeGrid();
			return;
		}

		boolean chagedPhase = phase != Phase.INSERTING;
		final List<Pair<Integer, Integer>> inserted = new LinkedList<>();

		DirectBeltInputBehaviour behaviour = getTargetingBelt();
		for (Entry<Pair<Integer, Integer>, ItemStack> entry : groupedItems.grid.entrySet()) {
			Pair<Integer, Integer> pair = entry.getKey();
			ItemStack stack = entry.getValue();
			BlockFace face = getTargetFace(level, worldPosition, getBlockState());

			ItemStack remainder = behaviour == null ? inserting.insert(stack.copy())
				: behaviour.handleInsertion(stack, face.getFace(), false);
			if (!remainder.isEmpty()) {
				stack.setCount(remainder.getCount());
				continue;
			}

			inserted.add(pair);
		}

		inserted.forEach(groupedItems.grid::remove);
		if (groupedItems.grid.isEmpty())
			ejectWholeGrid();
		else
			phase = Phase.INSERTING;
		if (!inserted.isEmpty() || chagedPhase)
			sendData();
	}

	public void ejectWholeGrid() {
		List<MechanicalCrafterBlockEntity> chain = RecipeGridHandler.getAllCraftersOfChain(this);
		if (chain == null)
			return;
		chain.forEach(MechanicalCrafterBlockEntity::eject);
	}

	public void eject() {
		BlockState blockState = getBlockState();
		boolean present = AllBlocks.MECHANICAL_CRAFTER.has(blockState);
		Vec3 vec = present ? Vec3.atLowerCornerOf(blockState.getValue(HORIZONTAL_FACING)
			.getNormal())
			.scale(.75f) : Vec3.ZERO;
		Vec3 ejectPos = VecHelper.getCenterOf(worldPosition)
			.add(vec);
		groupedItems.grid.forEach((pair, stack) -> dropItem(ejectPos, stack));
		if (!inventory.getItem(0)
			.isEmpty())
			dropItem(ejectPos, inventory.getItem(0));
		phase = Phase.IDLE;
		groupedItems = new GroupedItems();
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		sendData();
	}

	public void dropItem(Vec3 ejectPos, ItemStack stack) {
		ItemEntity itemEntity = new ItemEntity(level, ejectPos.x, ejectPos.y, ejectPos.z, stack);
		itemEntity.setDefaultPickUpDelay();
		level.addFreshEntity(itemEntity);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide && !isVirtual())
			return;
		if (phase == Phase.IDLE && craftingItemPresent())
			checkCompletedRecipe(false);
		if (phase == Phase.INSERTING)
			tryInsert();
	}

	public boolean craftingItemPresent() {
		return !inventory.getItem(0)
			.isEmpty();
	}

	public boolean craftingItemOrCoverPresent() {
		return !inventory.getItem(0)
			.isEmpty() || covered;
	}

	protected void checkCompletedRecipe(boolean poweredStart) {
		if (getSpeed() == 0)
			return;
		if (level.isClientSide && !isVirtual())
			return;
		List<MechanicalCrafterBlockEntity> chain = RecipeGridHandler.getAllCraftersOfChainIf(this,
			poweredStart ? MechanicalCrafterBlockEntity::craftingItemPresent
				: MechanicalCrafterBlockEntity::craftingItemOrCoverPresent,
			poweredStart);
		if (chain == null)
			return;
		chain.forEach(MechanicalCrafterBlockEntity::begin);
	}

	protected void begin() {
		phase = Phase.ACCEPTING;
		groupedItems = new GroupedItems(inventory.getItem(0));
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		if (RecipeGridHandler.getPrecedingCrafters(this)
			.isEmpty()) {
			phase = Phase.ASSEMBLING;
			countDown = 500;
		}
		sendData();
	}

	protected void continueIfAllPrecedingFinished() {
		List<MechanicalCrafterBlockEntity> preceding = RecipeGridHandler.getPrecedingCrafters(this);
		if (preceding == null) {
			ejectWholeGrid();
			return;
		}

		for (MechanicalCrafterBlockEntity blockEntity : preceding)
			if (blockEntity.phase != Phase.WAITING)
				return;

		phase = Phase.ASSEMBLING;
		countDown = Math.max(100, getCountDownSpeed() + 1);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return invSupplier.cast();
		return super.getCapability(cap, side);
	}

	public void connectivityChanged() {
		reRender = true;
		sendData();
		invSupplier.invalidate();
		invSupplier = LazyOptional.of(() -> input.getItemHandler(level, worldPosition));
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setScriptedResult(ItemStack scriptedResult) {
		this.scriptedResult = scriptedResult;
	}

}
