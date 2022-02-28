package com.simibubi.create.content.logistics.trains.management;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackGraphHelper;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.signal.EdgeData;
import com.simibubi.create.content.logistics.trains.management.signal.SingleTileEdgePoint;
import com.simibubi.create.content.logistics.trains.management.signal.TrackEdgePoint;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TrackTargetingBehaviour<T extends TrackEdgePoint> extends TileEntityBehaviour {

	public static final BehaviourType<TrackTargetingBehaviour<?>> TYPE = new BehaviourType<>();

	private BlockPos targetTrack;
	private AxisDirection targetDirection;
	private UUID id;

	private CompoundTag migrationData;
	private EdgePointType<T> edgePointType;
	private T edgePoint;

	public TrackTargetingBehaviour(SmartTileEntity te, EdgePointType<T> edgePointType) {
		super(te);
		this.edgePointType = edgePointType;
		targetDirection = AxisDirection.POSITIVE;
		targetTrack = BlockPos.ZERO;
		id = UUID.randomUUID();
		migrationData = null;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.putUUID("Id", id);
		nbt.put("TargetTrack", NbtUtils.writeBlockPos(targetTrack));
		nbt.putBoolean("TargetDirection", targetDirection == AxisDirection.POSITIVE);
		if (migrationData != null && !clientPacket)
			nbt.put("Migrate", migrationData);
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		UUID prevId = id;
		id = nbt.getUUID("Id");
		targetTrack = NbtUtils.readBlockPos(nbt.getCompound("TargetTrack"));
		targetDirection = nbt.getBoolean("TargetDirection") ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		if (nbt.contains("Migrate"))
			migrationData = nbt.getCompound("Migrate");
		if (clientPacket && !prevId.equals(id))
			edgePoint = null;
		super.read(nbt, clientPacket);
	}

	@Nullable
	public T getEdgePoint() {
		return edgePoint;
	}

	public void invalidateEdgePoint(CompoundTag migrationData) {
		this.migrationData = migrationData;
		id = UUID.randomUUID();
		edgePoint = null;
		tileEntity.sendData();
	}

	@Override
	public void tick() {
		super.tick();
		if (edgePoint == null)
			edgePoint = createEdgePoint();
	}

	@SuppressWarnings("unchecked")
	public T createEdgePoint() {
		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values()) { // TODO thread breach
			T point = trackGraph.getPoint(edgePointType, id);
			if (point == null)
				continue;
			return point;
		}

		if (getWorld().isClientSide)
			return null;
		if (!hasValidTrack())
			return null;
		GraphLocation loc = determineGraphLocation();
		if (loc == null)
			return null;

		TrackGraph graph = loc.graph;
		TrackNode node1 = graph.locateNode(loc.edge.getFirst());
		TrackNode node2 = graph.locateNode(loc.edge.getSecond());
		TrackEdge edge = graph.getConnectionsFrom(node1)
			.get(node2);

		boolean front = getTargetDirection() == AxisDirection.POSITIVE;

		if (edge == null)
			return null;

		EdgeData signalData = edge.getEdgeData();
		if (signalData.hasPoints()) {
			for (EdgePointType<?> otherType : EdgePointType.TYPES.values()) {
				TrackEdgePoint otherPoint = signalData.get(otherType, node1, node2, edge, loc.position);
				if (otherPoint == null)
					continue;
				if (otherType != edgePointType) {
					if (!otherPoint.canCoexistWith(edgePointType, front))
						return null;
					continue;
				}
				if (!otherPoint.canMerge())
					return null;
				otherPoint.tileAdded(getPos(), front);
				id = otherPoint.getId();
				tileEntity.setChanged();
				return (T) otherPoint;
			}
		}

		T point = edgePointType.create();
		point.setId(id);

		if (point instanceof SingleTileEdgePoint step) {
			point.setLocation(loc.edge, loc.position);
			if (migrationData != null) {
				step.read(migrationData, true);
				migrationData = null;
				tileEntity.setChanged();
			}
		} else
			point.setLocation(front ? loc.edge : loc.edge.swap(),
				front ? loc.position : edge.getLength(node1, node2) - loc.position);

		point.tileAdded(getPos(), front);
		loc.graph.addPoint(edgePointType, point);
		return point;
	}

	@Override
	public void remove() {
		if (edgePoint != null && !getWorld().isClientSide)
			edgePoint.tileRemoved(getPos(), getTargetDirection() == AxisDirection.POSITIVE);
		super.remove();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean hasValidTrack() {
		return getTrackBlockState().getBlock() instanceof ITrackBlock;
	}

	public ITrackBlock getTrack() {
		return (ITrackBlock) getTrackBlockState().getBlock();
	}

	public BlockState getTrackBlockState() {
		return getWorld().getBlockState(getGlobalPosition());
	}

	public BlockPos getGlobalPosition() {
		return targetTrack.offset(tileEntity.getBlockPos());
	}

	public AxisDirection getTargetDirection() {
		return targetDirection;
	}

	public GraphLocation determineGraphLocation() {
		Level level = getWorld();
		BlockPos pos = getGlobalPosition();
		BlockState state = getTrackBlockState();
		return TrackGraphHelper.getGraphLocationAt(level, pos, getTargetDirection(),
			getTrack().getTrackAxes(level, pos, state)
				.get(0));
	}

	public static enum RenderedTrackOverlayType {
		STATION, SIGNAL, DUAL_SIGNAL;
	}

	@OnlyIn(Dist.CLIENT)
	public static void render(LevelAccessor level, BlockPos pos, AxisDirection direction, int tintColor, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay, RenderedTrackOverlayType type) {
		BlockState trackState = level.getBlockState(pos);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		ms.pushPose();
		ms.translate(pos.getX(), pos.getY(), pos.getZ());

		ITrackBlock track = (ITrackBlock) block;
		SuperByteBuffer sbb =
			CachedBufferer.partial(track.prepareTrackOverlay(level, pos, trackState, direction, ms, type), trackState);
		sbb.light(LevelRenderer.getLightColor(level, pos));
		sbb.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		ms.popPose();
	}

}
