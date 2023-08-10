package com.simibubi.create.content.trains.track;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackGraphHelper;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.render.CachedPartialBuffers;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.worldWrappers.SchematicWorld;
import net.createmod.ponder.foundation.PonderWorld;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TrackTargetingBehaviour<T extends TrackEdgePoint> extends BlockEntityBehaviour {

	public static final BehaviourType<TrackTargetingBehaviour<?>> TYPE = new BehaviourType<>();

	private BlockPos targetTrack;
	private BezierTrackPointLocation targetBezier;
	private AxisDirection targetDirection;
	private UUID id;

	private Vec3 prevDirection;
	private Vec3 rotatedDirection;

	private CompoundTag migrationData;
	private EdgePointType<T> edgePointType;
	private T edgePoint;
	private boolean orthogonal;

	public TrackTargetingBehaviour(SmartBlockEntity be, EdgePointType<T> edgePointType) {
		super(be);
		this.edgePointType = edgePointType;
		targetDirection = AxisDirection.POSITIVE;
		targetTrack = BlockPos.ZERO;
		id = UUID.randomUUID();
		migrationData = null;
		orthogonal = false;
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.putUUID("Id", id);
		nbt.put("TargetTrack", NbtUtils.writeBlockPos(targetTrack));
		nbt.putBoolean("Ortho", orthogonal);
		nbt.putBoolean("TargetDirection", targetDirection == AxisDirection.POSITIVE);
		if (rotatedDirection != null)
			nbt.put("RotatedAxis", VecHelper.writeNBT(rotatedDirection));
		if (prevDirection != null)
			nbt.put("PrevAxis", VecHelper.writeNBT(prevDirection));
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
		id = nbt.contains("Id") ? nbt.getUUID("Id") : UUID.randomUUID();
		targetTrack = NbtUtils.readBlockPos(nbt.getCompound("TargetTrack"));
		targetDirection = nbt.getBoolean("TargetDirection") ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		orthogonal = nbt.getBoolean("Ortho");
		if (nbt.contains("PrevAxis"))
			prevDirection = VecHelper.readNBT(nbt.getList("PrevAxis", Tag.TAG_DOUBLE));
		if (nbt.contains("RotatedAxis"))
			rotatedDirection = VecHelper.readNBT(nbt.getList("RotatedAxis", Tag.TAG_DOUBLE));
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
		blockEntity.sendData();
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
		TrackGraphLocation loc = determineGraphLocation();
		if (loc == null)
			return null;

		TrackGraph graph = loc.graph;
		TrackNode node1 = graph.locateNode(loc.edge.getFirst());
		TrackNode node2 = graph.locateNode(loc.edge.getSecond());
		TrackEdge edge = graph.getConnectionsFrom(node1)
			.get(node2);
		if (edge == null)
			return null;

		T point = edgePointType.create();
		boolean front = getTargetDirection() == AxisDirection.POSITIVE;

		prevDirection = edge.getDirectionAt(loc.position)
			.scale(front ? -1 : 1);

		if (rotatedDirection != null) {
			double dot = prevDirection.dot(rotatedDirection);
			if (dot < -.85f) {
				rotatedDirection = null;
				targetDirection = targetDirection.opposite();
				return null;
			}

			rotatedDirection = null;
		}

		double length = edge.getLength();
		CompoundTag data = migrationData;
		migrationData = null;

		{
			orthogonal = targetBezier == null;
			Vec3 direction = edge.getDirection(true);
			int nonZeroComponents = 0;
			for (Axis axis : Iterate.axes)
				nonZeroComponents += direction.get(axis) != 0 ? 1 : 0;
			orthogonal &= nonZeroComponents <= 1;
		}

		EdgeData signalData = edge.getEdgeData();
		if (signalData.hasPoints()) {
			for (EdgePointType<?> otherType : EdgePointType.TYPES.values()) {
				TrackEdgePoint otherPoint = signalData.get(otherType, loc.position);
				if (otherPoint == null)
					continue;
				if (otherType != edgePointType) {
					if (!otherPoint.canCoexistWith(edgePointType, front))
						return null;
					continue;
				}
				if (!otherPoint.canMerge())
					return null;
				otherPoint.blockEntityAdded(blockEntity, front);
				id = otherPoint.getId();
				blockEntity.notifyUpdate();
				return (T) otherPoint;
			}
		}

		if (data != null)
			point.read(data, true, DimensionPalette.read(data));

		point.setId(id);
		boolean reverseEdge = front || point instanceof SingleBlockEntityEdgePoint;
		point.setLocation(reverseEdge ? loc.edge : loc.edge.swap(), reverseEdge ? loc.position : length - loc.position);
		point.blockEntityAdded(blockEntity, front);
		loc.graph.addPoint(edgePointType, point);
		blockEntity.sendData();
		return point;
	}

	@Override
	public void destroy() {
		super.destroy();
		if (edgePoint != null && !getWorld().isClientSide)
			edgePoint.blockEntityRemoved(getPos(), getTargetDirection() == AxisDirection.POSITIVE);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean isOnCurve() {
		return targetBezier != null;
	}

	public boolean isOrthogonal() {
		return orthogonal;
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
		return targetTrack.offset(blockEntity.getBlockPos());
	}

	public BlockPos getPositionForMapMarker() {
		BlockPos target = targetTrack.offset(blockEntity.getBlockPos());
		if (targetBezier != null && getWorld().getBlockEntity(target) instanceof TrackBlockEntity tbe) {
			BezierConnection bc = tbe.getConnections()
				.get(targetBezier.curveTarget());
			if (bc == null)
				return target;
			double length = Mth.floor(bc.getLength() * 2);
			int seg = targetBezier.segment() + 1;
			double t = seg / length;
			return new BlockPos(bc.getPosition(t));
		}
		return target;
	}

	public AxisDirection getTargetDirection() {
		return targetDirection;
	}

	public BezierTrackPointLocation getTargetBezier() {
		return targetBezier;
	}

	public TrackGraphLocation determineGraphLocation() {
		Level level = getWorld();
		BlockPos pos = getGlobalPosition();
		BlockState state = getTrackBlockState();
		ITrackBlock track = getTrack();
		List<Vec3> trackAxes = track.getTrackAxes(level, pos, state);
		AxisDirection targetDirection = getTargetDirection();

		return targetBezier != null
			? TrackGraphHelper.getBezierGraphLocationAt(level, pos, targetDirection, targetBezier)
			: TrackGraphHelper.getGraphLocationAt(level, pos, targetDirection, trackAxes.get(0));
	}

	public static enum RenderedTrackOverlayType {
		STATION, SIGNAL, DUAL_SIGNAL, OBSERVER;
	}

	@OnlyIn(Dist.CLIENT)
	public static void render(LevelAccessor level, BlockPos pos, AxisDirection direction,
		BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
		RenderedTrackOverlayType type, float scale) {
		if (level instanceof SchematicWorld && !(level instanceof PonderWorld))
			return;

		BlockState trackState = level.getBlockState(pos);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		ms.pushPose();
		ITrackBlock track = (ITrackBlock) block;
		PartialModel partial = track.prepareTrackOverlay(level, pos, trackState, bezier, direction, ms, type);
		if (partial != null)
			CachedPartialBuffers.partial(partial, trackState)
				.translate(.5, 0, .5)
				.scale(scale)
				.translate(-.5, 0, -.5)
				.light(LevelRenderer.getLightColor(level, pos))
				.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
		ms.popPose();
	}

	public void transform(StructureTransform transform) {
		id = UUID.randomUUID();
		targetTrack = transform.applyWithoutOffset(targetTrack);
		if (prevDirection != null)
			rotatedDirection = transform.applyWithoutOffsetUncentered(prevDirection);
		if (targetBezier != null)
			targetBezier = new BezierTrackPointLocation(transform.applyWithoutOffset(targetBezier.curveTarget()
				.subtract(getPos()))
				.offset(getPos()), targetBezier.segment());
		blockEntity.notifyUpdate();
	}

}
