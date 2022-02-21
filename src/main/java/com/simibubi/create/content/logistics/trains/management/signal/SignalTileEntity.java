package com.simibubi.create.content.logistics.trains.management.signal;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.management.GraphLocation;
import com.simibubi.create.content.logistics.trains.management.TrackTargetingBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.TickPriority;

public class SignalTileEntity extends SmartTileEntity {

	public static enum OverlayState {
		RENDER, SKIP, DUAL
	}

	public static enum SignalState {
		RED, GREEN, INVALID, TRAIN_ENTERING;

		public boolean isRedLight(float renderTime) {
			return this == RED || this == INVALID && renderTime % 40 < 3;
		}

		public boolean isGreenLight(float renderTime) {
			return this == GREEN || this == TRAIN_ENTERING;
		}
	}

	public UUID id;

	private SignalState state;
	private OverlayState overlay;
	private int switchToRedAfterTrainEntered;

	public SignalTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		id = UUID.randomUUID();
		this.state = SignalState.INVALID;
		this.overlay = OverlayState.SKIP;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putUUID("Id", id);
		NBTHelper.writeEnum(tag, "State", state);
		NBTHelper.writeEnum(tag, "Overlay", overlay);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		id = tag.getUUID("Id");
		state = NBTHelper.readEnum(tag, "State", SignalState.class);
		overlay = NBTHelper.readEnum(tag, "Overlay", OverlayState.class);
		invalidateRenderBoundingBox();
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
		behaviours.add(new TrackTargetingBehaviour(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide)
			return;
		SignalBoundary boundary = getOrCreateSignalBoundary();
		if (boundary == null) {
			enterState(SignalState.INVALID);
			setOverlay(OverlayState.RENDER);
			return;
		}
		enterState(boundary.getStateFor(worldPosition));
		setOverlay(boundary.getOverlayFor(worldPosition));
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		if (!getTarget().hasValidTrack() || level.isClientSide) {
			super.setRemovedNotDueToChunkUnload();
			return;
		}

		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values()) {
			SignalBoundary signal = trackGraph.getSignal(id);
			if (signal == null)
				continue;
			for (boolean front : Iterate.trueAndFalse)
				signal.signals.get(front)
					.remove(worldPosition);
			if (signal.signals.getFirst()
				.isEmpty()
				&& signal.signals.getSecond()
					.isEmpty())
				trackGraph.removeSignal(id);
		}

		super.setRemovedNotDueToChunkUnload();
	}

	public SignalBoundary getOrCreateSignalBoundary() {
		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values()) {
			SignalBoundary signal = trackGraph.getSignal(id);
			if (signal == null)
				continue;
			return signal;
		}

		if (level.isClientSide)
			return null;

		TrackTargetingBehaviour target = getTarget();
		if (!target.hasValidTrack())
			return null;
		GraphLocation loc = target.determineGraphLocation();
		if (loc == null)
			return null;

		TrackGraph graph = loc.graph;
		TrackNode node1 = graph.locateNode(loc.edge.getFirst());
		TrackNode node2 = graph.locateNode(loc.edge.getSecond());
		TrackEdge edge = graph.getConnectionsFrom(node1)
			.get(node2);
		boolean positive = target.getTargetDirection() == AxisDirection.POSITIVE;

		if (edge == null)
			return null;

		EdgeData signalData = edge.getEdgeData();
		if (signalData.hasBoundaries()) {
			SignalBoundary nextBoundary = signalData.nextBoundary(node1, node2, edge, loc.position - .25f);
			if (nextBoundary != null && Mth.equal(nextBoundary.getLocationOn(node1, node2, edge), loc.position)) {
				nextBoundary.signals.get(positive)
					.add(worldPosition);
				id = nextBoundary.id;
				setChanged();
				return nextBoundary;
			}
		}

		SignalBoundary signal = new SignalBoundary(id, worldPosition, positive);
		signal.setLocation(positive ? loc.edge : loc.edge.swap(),
			positive ? loc.position : edge.getLength(node1, node2) - loc.position);
		graph.addSignal(signal);
		setChanged();
		return signal;
	}

	public TrackTargetingBehaviour getTarget() {
		return getBehaviour(TrackTargetingBehaviour.TYPE);
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
		switchToRedAfterTrainEntered = state == SignalState.GREEN ? 15 : 0;
		notifyUpdate();
		scheduleBlockTick();
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition, getTarget().getGlobalPosition()).inflate(2);
	}

}
