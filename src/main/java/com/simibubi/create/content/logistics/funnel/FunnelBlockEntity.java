package com.simibubi.create.content.logistics.funnel;

import java.lang.ref.WeakReference;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class FunnelBlockEntity extends SmartBlockEntity implements IHaveHoveringInformation {

	private FilteringBehaviour filtering;
	private InvManipulationBehaviour invManipulation;
	private VersionedInventoryTrackerBehaviour invVersionTracker;
	private int extractionCooldown;

	private WeakReference<ItemEntity> lastObserved; // In-world Extractors only

	LerpedFloat flap;

	static enum Mode {
		INVALID, PAUSED, COLLECT, PUSHING_TO_BELT, TAKING_FROM_BELT, EXTRACT
	}

	public FunnelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		extractionCooldown = 0;
		flap = createChasingFlap();
	}

	public Mode determineCurrentMode() {
		BlockState state = getBlockState();
		if (!FunnelBlock.isFunnel(state))
			return Mode.INVALID;
		if (state.getOptionalValue(BlockStateProperties.POWERED)
			.orElse(false))
			return Mode.PAUSED;
		if (state.getBlock() instanceof BeltFunnelBlock) {
			Shape shape = state.getValue(BeltFunnelBlock.SHAPE);
			if (shape == Shape.PULLING)
				return Mode.TAKING_FROM_BELT;
			if (shape == Shape.PUSHING)
				return Mode.PUSHING_TO_BELT;

			BeltBlockEntity belt = BeltHelper.getSegmentBE(level, worldPosition.below());
			if (belt != null)
				return belt.getMovementFacing() == state.getValue(BeltFunnelBlock.HORIZONTAL_FACING)
					? Mode.PUSHING_TO_BELT
					: Mode.TAKING_FROM_BELT;
			return Mode.INVALID;
		}
		if (state.getBlock() instanceof FunnelBlock)
			return state.getValue(FunnelBlock.EXTRACTING) ? Mode.EXTRACT : Mode.COLLECT;

		return Mode.INVALID;
	}

	@Override
	public void tick() {
		super.tick();
		flap.tickChaser();
		Mode mode = determineCurrentMode();
		if (level.isClientSide)
			return;

		// Redstone resets the extraction cooldown
		if (mode == Mode.PAUSED)
			extractionCooldown = 0;
		if (mode == Mode.TAKING_FROM_BELT)
			return;

		if (extractionCooldown > 0) {
			extractionCooldown--;
			return;
		}

		if (mode == Mode.PUSHING_TO_BELT)
			activateExtractingBeltFunnel();
		if (mode == Mode.EXTRACT)
			activateExtractor();
	}

	private void activateExtractor() {
		if (invVersionTracker.stillWaiting(invManipulation))
			return;
		
		BlockState blockState = getBlockState();
		Direction facing = AbstractFunnelBlock.getFunnelFacing(blockState);

		if (facing == null)
			return;

		boolean trackingEntityPresent = true;
		AABB area = getEntityOverflowScanningArea();

		// Check if last item is still blocking the extractor
		if (lastObserved == null) {
			trackingEntityPresent = false;
		} else {
			ItemEntity lastEntity = lastObserved.get();
			if (lastEntity == null || !lastEntity.isAlive() || !lastEntity.getBoundingBox()
				.intersects(area)) {
				trackingEntityPresent = false;
				lastObserved = null;
			}
		}

		if (trackingEntityPresent)
			return;

		// Find other entities blocking the extract (only if necessary)
		int amountToExtract = getAmountToExtract();
		ExtractionCountMode mode = getModeToExtract();
		ItemStack stack = invManipulation.simulate()
			.extract(mode, amountToExtract);
		if (stack.isEmpty()) {
			invVersionTracker.awaitNewVersion(invManipulation);
			return;
		}
		for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area)) {
			lastObserved = new WeakReference<>(itemEntity);
			return;
		}

		// Extract
		stack = invManipulation.extract(mode, amountToExtract);
		if (stack.isEmpty())
			return;

		flap(false);
		onTransfer(stack);

		Vec3 outputPos = VecHelper.getCenterOf(worldPosition);
		boolean vertical = facing.getAxis()
			.isVertical();
		boolean up = facing == Direction.UP;

		outputPos = outputPos.add(Vec3.atLowerCornerOf(facing.getNormal())
			.scale(vertical ? up ? .15f : .5f : .25f));
		if (!vertical)
			outputPos = outputPos.subtract(0, .45f, 0);

		Vec3 motion = Vec3.ZERO;
		if (up)
			motion = new Vec3(0, 4 / 16f, 0);

		ItemEntity item = new ItemEntity(level, outputPos.x, outputPos.y, outputPos.z, stack.copy());
		item.setDefaultPickUpDelay();
		item.setDeltaMovement(motion);
		level.addFreshEntity(item);
		lastObserved = new WeakReference<>(item);

		startCooldown();
	}

	static final AABB coreBB = new AABB(VecHelper.CENTER_OF_ORIGIN, VecHelper.CENTER_OF_ORIGIN).inflate(.75f);

	private AABB getEntityOverflowScanningArea() {
		Direction facing = AbstractFunnelBlock.getFunnelFacing(getBlockState());
		AABB bb = coreBB.move(worldPosition);
		if (facing == null || facing == Direction.UP)
			return bb;
		return bb.expandTowards(0, -1, 0);
	}

	private void activateExtractingBeltFunnel() {
		if (invVersionTracker.stillWaiting(invManipulation))
			return;

		BlockState blockState = getBlockState();
		Direction facing = blockState.getValue(BeltFunnelBlock.HORIZONTAL_FACING);
		DirectBeltInputBehaviour inputBehaviour =
			BlockEntityBehaviour.get(level, worldPosition.below(), DirectBeltInputBehaviour.TYPE);

		if (inputBehaviour == null)
			return;
		if (!inputBehaviour.canInsertFromSide(facing))
			return;
		if (inputBehaviour.isOccupied(facing))
			return;

		int amountToExtract = getAmountToExtract();
		ExtractionCountMode mode = getModeToExtract();
		MutableBoolean deniedByInsertion = new MutableBoolean(false);
		ItemStack stack = invManipulation.extract(mode, amountToExtract, s -> {
			ItemStack handleInsertion = inputBehaviour.handleInsertion(s, facing, true);
			if (handleInsertion.isEmpty())
				return true;
			deniedByInsertion.setTrue();
			return false;
		});
		if (stack.isEmpty()) {
			if (deniedByInsertion.isFalse())
				invVersionTracker.awaitNewVersion(invManipulation.getInventory());
			return;
		}
		flap(false);
		onTransfer(stack);
		inputBehaviour.handleInsertion(stack, facing, false);
		startCooldown();
	}

	public int getAmountToExtract() {
		if (!supportsAmountOnFilter())
			return 64;
		int amountToExtract = invManipulation.getAmountFromFilter();
		if (!filtering.isActive())
			amountToExtract = 1;
		return amountToExtract;
	}

	public ExtractionCountMode getModeToExtract() {
		if (!supportsAmountOnFilter() || !filtering.isActive())
			return ExtractionCountMode.UPTO;
		return invManipulation.getModeFromFilter();
	}

	private int startCooldown() {
		return extractionCooldown = AllConfigs.server().logistics.defaultExtractionTimer.get();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		invManipulation =
			new InvManipulationBehaviour(this, (w, p, s) -> new BlockFace(p, AbstractFunnelBlock.getFunnelFacing(s)
				.getOpposite()));
		behaviours.add(invManipulation);
		
		behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));

		filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning());
		filtering.showCountWhen(this::supportsAmountOnFilter);
		filtering.onlyActiveWhen(this::supportsFiltering);
		filtering.withCallback($ -> invVersionTracker.reset());
		behaviours.add(filtering);
		
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput)
			.setInsertionHandler(this::handleDirectBeltInput));
		registerAwardables(behaviours, AllAdvancements.FUNNEL);
	}

	private boolean supportsAmountOnFilter() {
		BlockState blockState = getBlockState();
		boolean beltFunnelsupportsAmount = false;
		if (blockState.getBlock() instanceof BeltFunnelBlock) {
			Shape shape = blockState.getValue(BeltFunnelBlock.SHAPE);
			if (shape == Shape.PUSHING)
				beltFunnelsupportsAmount = true;
			else
				beltFunnelsupportsAmount = BeltHelper.getSegmentBE(level, worldPosition.below()) != null;
		}
		boolean extractor = blockState.getBlock() instanceof FunnelBlock && blockState.getValue(FunnelBlock.EXTRACTING);
		return beltFunnelsupportsAmount || extractor;
	}

	private boolean supportsDirectBeltInput(Direction side) {
		BlockState blockState = getBlockState();
		if (blockState == null)
			return false;
		if (!(blockState.getBlock() instanceof FunnelBlock))
			return false;
		if (blockState.getValue(FunnelBlock.EXTRACTING))
			return false;
		return FunnelBlock.getFunnelFacing(blockState) == Direction.UP;
	}

	private boolean supportsFiltering() {
		BlockState blockState = getBlockState();
		return AllBlocks.BRASS_BELT_FUNNEL.has(blockState) || AllBlocks.BRASS_FUNNEL.has(blockState);
	}

	private ItemStack handleDirectBeltInput(TransportedItemStack stack, Direction side, boolean simulate) {
		ItemStack inserted = stack.stack;
		if (!filtering.test(inserted))
			return inserted;
		if (determineCurrentMode() == Mode.PAUSED)
			return inserted;
		if (simulate)
			invManipulation.simulate();
		if (!simulate)
			onTransfer(inserted);
		return invManipulation.insert(inserted);
	}

	public void flap(boolean inward) {
		if (!level.isClientSide) {
			AllPackets.getChannel()
				.send(packetTarget(), new FunnelFlapPacket(this, inward));
		} else {
			flap.setValue(inward ? 1 : -1);
			AllSoundEvents.FUNNEL_FLAP.playAt(level, worldPosition, 1, 1, true);
		}
	}

	public boolean hasFlap() {
		BlockState blockState = getBlockState();
		if (!AbstractFunnelBlock.getFunnelFacing(blockState)
			.getAxis()
			.isHorizontal())
			return false;
		return true;
	}

	public float getFlapOffset() {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BeltFunnelBlock))
			return -1 / 16f;
		switch (blockState.getValue(BeltFunnelBlock.SHAPE)) {
		default:
		case RETRACTED:
			return 0;
		case EXTENDED:
			return 8 / 16f;
		case PULLING:
		case PUSHING:
			return -2 / 16f;
		}
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("TransferCooldown", extractionCooldown);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		extractionCooldown = compound.getInt("TransferCooldown");

		if (clientPacket)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	public void onTransfer(ItemStack stack) {
		AllBlocks.SMART_OBSERVER.get()
			.onFunnelTransfer(level, worldPosition, stack);
		award(AllAdvancements.FUNNEL);
	}

	private LerpedFloat createChasingFlap() {
		return LerpedFloat.linear()
			.startWithValue(.25f)
			.chase(0, .05f, Chaser.EXP);
	}

}
