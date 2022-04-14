package com.simibubi.create.content.logistics.trains.management.edgePoint;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackGraphHelper;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SingleTileEdgePoint;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.TrackEdgePoint;
import com.simibubi.create.content.logistics.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.logistics.trains.track.TrackTileEntity;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.Couple;

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
	private BezierTrackPointLocation targetBezier;
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
		if (targetBezier != null) {
			CompoundTag bezierNbt = new CompoundTag();
			bezierNbt.putInt("Segment", targetBezier.segment());
			bezierNbt.put("Key", NbtUtils.writeBlockPos(targetBezier.curveTarget()
				.subtract(getPos())));
			nbt.put("Bezier", bezierNbt);
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		id = nbt.getUUID("Id");
		targetTrack = NbtUtils.readBlockPos(nbt.getCompound("TargetTrack"));
		targetDirection = nbt.getBoolean("TargetDirection") ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		if (nbt.contains("Migrate"))
			migrationData = nbt.getCompound("Migrate");
		if (clientPacket)
			edgePoint = null;
		if (nbt.contains("Bezier")) {
			CompoundTag bezierNbt = nbt.getCompound("Bezier");
			BlockPos key = NbtUtils.readBlockPos(bezierNbt.getCompound("Key"));
			targetBezier = new BezierTrackPointLocation(bezierNbt.contains("FromStack") ? key : key.offset(getPos()),
				bezierNbt.getInt("Segment"));
		}
		super.read(nbt, clientPacket);
	}

	@Nullable
	public T getEdgePoint() {
		return edgePoint;
	}

	public void invalidateEdgePoint(CompoundTag migrationData) {
		this.migrationData = migrationData;
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
		Level level = getWorld();
		boolean isClientSide = level.isClientSide;
		if (migrationData == null || isClientSide)
			for (TrackGraph trackGraph : Create.RAILWAYS.sided(level).trackNetworks.values()) {
				T point = trackGraph.getPoint(edgePointType, id);
				if (point == null)
					continue;
				return point;
			}

		if (isClientSide)
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

		double length = edge.getLength(node1, node2);
		CompoundTag data = migrationData;
		migrationData = null;

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
				otherPoint.tileAdded(tileEntity, front);
				id = otherPoint.getId();
				tileEntity.setChanged();
				return (T) otherPoint;
			}
		}

		T point = edgePointType.create();
		boolean reverseEdge = front || point instanceof SingleTileEdgePoint;

		if (data != null)
			point.read(data, true);

		point.setId(id);
		point.setLocation(reverseEdge ? loc.edge : loc.edge.swap(), reverseEdge ? loc.position : length - loc.position);
		point.tileAdded(tileEntity, front);
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

	public BezierTrackPointLocation getTargetBezier() {
		return targetBezier;
	}

	public GraphLocation determineGraphLocation() {
		if (targetBezier != null)
			return determineBezierGraphLocation();
		Level level = getWorld();
		BlockPos pos = getGlobalPosition();
		BlockState state = getTrackBlockState();
		return TrackGraphHelper.getGraphLocationAt(level, pos, getTargetDirection(),
			getTrack().getTrackAxes(level, pos, state)
				.get(0));
	}

	public GraphLocation determineBezierGraphLocation() {
		Level level = getWorld();
		BlockPos pos = getGlobalPosition();
		BlockState state = getTrackBlockState();
		if (!(state.getBlock() instanceof ITrackBlock track))
			return null;
		if (!(level.getBlockEntity(pos) instanceof TrackTileEntity trackTE))
			return null;
		BezierConnection bc = trackTE.getConnections()
			.get(targetBezier.curveTarget());
		if (bc == null || !bc.isPrimary())
			return null;

		TrackNodeLocation targetLoc = new TrackNodeLocation(bc.starts.getSecond());
		for (DiscoveredLocation location : track.getConnected(level, pos, state, true, null)) {
			TrackGraph graph = Create.RAILWAYS.sided(level)
				.getGraph(level, location);
			if (graph == null)
				continue;
			TrackNode targetNode = graph.locateNode(targetLoc);
			if (targetNode == null)
				continue;
			TrackNode node = graph.locateNode(location);
			TrackEdge edge = graph.getConnectionsFrom(node)
				.get(targetNode);
			if (edge == null)
				continue;

			GraphLocation graphLocation = new GraphLocation();
			graphLocation.graph = graph;
			graphLocation.edge = Couple.create(location, targetLoc);
			graphLocation.position = (targetBezier.segment() + 1) / 2f;
			if (targetDirection == AxisDirection.POSITIVE) {
				graphLocation.edge = graphLocation.edge.swap();
				graphLocation.position = edge.getLength(node, targetNode) - graphLocation.position;
			}

			return graphLocation;
		}

		return null;
	}

	public static enum RenderedTrackOverlayType {
		STATION, SIGNAL, DUAL_SIGNAL;
	}

	@OnlyIn(Dist.CLIENT)
	public static void render(LevelAccessor level, BlockPos pos, AxisDirection direction,
		BezierTrackPointLocation bezier, int tintColor, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
		RenderedTrackOverlayType type) {
		BlockState trackState = level.getBlockState(pos);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		ms.pushPose();
		ms.translate(pos.getX(), pos.getY(), pos.getZ());

		ITrackBlock track = (ITrackBlock) block;
		SuperByteBuffer sbb = CachedBufferer
			.partial(track.prepareTrackOverlay(level, pos, trackState, bezier, direction, ms, type), trackState);
		sbb.light(LevelRenderer.getLightColor(level, pos));
		sbb.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		ms.popPose();
	}

}
