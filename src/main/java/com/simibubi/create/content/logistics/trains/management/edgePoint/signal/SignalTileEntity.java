package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.ITransformableTE;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalBlock.SignalType;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.TickPriority;

public class SignalTileEntity extends SmartTileEntity implements ITransformableTE {

	public static enum OverlayState {
		RENDER, SKIP, DUAL
	}

	public static enum SignalState {
		RED, YELLOW, GREEN, INVALID;

		public boolean isRedLight(float renderTime) {
			return this == RED || this == INVALID && renderTime % 40 < 3;
		}

		public boolean isYellowLight(float renderTime) {
			return this == YELLOW;
		}

		public boolean isGreenLight(float renderTime) {
			return this == GREEN;
		}
	}

	public TrackTargetingBehaviour<SignalBoundary> edgePoint;

	private SignalState state;
	private OverlayState overlay;
	private int switchToRedAfterTrainEntered;

	public SignalTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.state = SignalState.INVALID;
		this.overlay = OverlayState.SKIP;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		NBTHelper.writeEnum(tag, "State", state);
		NBTHelper.writeEnum(tag, "Overlay", overlay);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		state = NBTHelper.readEnum(tag, "State", SignalState.class);
		overlay = NBTHelper.readEnum(tag, "Overlay", OverlayState.class);
		invalidateRenderBoundingBox();
	}

	@Nullable
	public SignalBoundary getSignal() {
		return edgePoint.getEdgePoint();
	}

	public boolean isPowered() {
		return state == SignalState.RED;
	}

	protected void scheduleBlockTick() {
		Block block = getBlockState().getBlock();
		if (!level.getBlockTicks()
			.willTickThisTick(worldPosition, block))
			level.scheduleTick(worldPosition, block, 2, TickPriority.NORMAL);
	}

	public void updatePowerAfterDelay() {
		level.blockUpdated(worldPosition, getBlockState().getBlock());
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.SIGNAL);
		behaviours.add(edgePoint);
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide)
			return;

		SignalBoundary boundary = getSignal();
		if (boundary == null) {
			enterState(SignalState.INVALID);
			setOverlay(OverlayState.RENDER);
			return;
		}

		getBlockState().getOptionalValue(SignalBlock.TYPE)
			.ifPresent(stateType -> {
				SignalType targetType = boundary.getTypeFor(worldPosition);
				if (stateType != targetType) {
					level.setBlock(worldPosition, getBlockState().setValue(SignalBlock.TYPE, targetType), 3);
					refreshBlockState();
				}
			});

		enterState(boundary.getStateFor(worldPosition));
		setOverlay(boundary.getOverlayFor(worldPosition));
	}

	public SignalState getState() {
		return state;
	}

	public OverlayState getOverlay() {
		return overlay;
	}

	public void setOverlay(OverlayState state) {
		if (this.overlay == state)
			return;
		this.overlay = state;
		notifyUpdate();
	}

	public void enterState(SignalState state) {
		if (switchToRedAfterTrainEntered > 0)
			switchToRedAfterTrainEntered--;
		if (this.state == state)
			return;
		if (state == SignalState.RED && switchToRedAfterTrainEntered > 0)
			return;
		this.state = state;
		switchToRedAfterTrainEntered = state == SignalState.GREEN || state == SignalState.YELLOW ? 15 : 0;
		notifyUpdate();
		scheduleBlockTick();
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition, edgePoint.getGlobalPosition()).inflate(2);
	}

	@Override
	public void transform(StructureTransform transform) {
		edgePoint.transform(transform);
	}

}
