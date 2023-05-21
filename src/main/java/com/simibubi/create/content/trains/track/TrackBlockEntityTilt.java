package com.simibubi.create.content.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelProperty;

public class TrackBlockEntityTilt {

	public static final ModelProperty<Double> ASCENDING_PROPERTY = new ModelProperty<>();

	public Optional<Double> smoothingAngle;
	private Couple<Pair<Vec3, Integer>> previousSmoothingHandles;

	private TrackBlockEntity blockEntity;

	public TrackBlockEntityTilt(TrackBlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		smoothingAngle = Optional.empty();
	}

	public void tryApplySmoothing() {
		if (smoothingAngle.isPresent())
			return;

		Couple<BezierConnection> discoveredSlopes = Couple.create(null, null);
		Vec3 axis = null;

		BlockState blockState = blockEntity.getBlockState();
		BlockPos worldPosition = blockEntity.getBlockPos();
		Level level = blockEntity.getLevel();

		if (!(blockState.getBlock()instanceof ITrackBlock itb))
			return;
		List<Vec3> axes = itb.getTrackAxes(level, worldPosition, blockState);
		if (axes.size() != 1)
			return;
		if (axes.get(0).y != 0)
			return;
		if (blockEntity.boundLocation != null)
			return;

		for (BezierConnection bezierConnection : blockEntity.connections.values()) {
			if (bezierConnection.starts.getFirst().y == bezierConnection.starts.getSecond().y)
				continue;
			Vec3 normedAxis = bezierConnection.axes.getFirst()
				.normalize();

			if (axis != null) {
				if (discoveredSlopes.getSecond() != null)
					return;
				if (normedAxis.dot(axis) > -1 + 1 / 64.0)
					return;
				discoveredSlopes.setSecond(bezierConnection);
				continue;
			}

			axis = normedAxis;
			discoveredSlopes.setFirst(bezierConnection);
		}

		if (discoveredSlopes.either(Objects::isNull))
			return;
		if (discoveredSlopes.getFirst().starts.getSecond().y > discoveredSlopes.getSecond().starts.getSecond().y)
			discoveredSlopes = discoveredSlopes.swap();

		Couple<Vec3> lowStarts = discoveredSlopes.getFirst().starts;
		Couple<Vec3> highStarts = discoveredSlopes.getSecond().starts;
		Vec3 lowestPoint = lowStarts.getSecond();
		Vec3 highestPoint = highStarts.getSecond();

		if (lowestPoint.y > lowStarts.getFirst().y)
			return;
		if (highestPoint.y < highStarts.getFirst().y)
			return;

		blockEntity.removeInboundConnections(false);
		blockEntity.connections.clear();
		TrackPropagator.onRailRemoved(level, worldPosition, blockState);

		double hDistance = discoveredSlopes.getFirst()
			.getLength()
			+ discoveredSlopes.getSecond()
				.getLength();
		Vec3 baseAxis = discoveredSlopes.getFirst().axes.getFirst();
		double baseAxisLength = baseAxis.x != 0 && baseAxis.z != 0 ? Math.sqrt(2) : 1;
		double vDistance = highestPoint.y - lowestPoint.y;
		double m = vDistance / (hDistance);

		Vec3 diff = highStarts.getFirst()
			.subtract(lowStarts.getFirst());
		boolean flipRotation = diff.dot(new Vec3(1, 0, 2).normalize()) <= 0;
		smoothingAngle = Optional.of(Math.toDegrees(Mth.atan2(m, 1)) * (flipRotation ? -1 : 1));

		int smoothingParam = Mth.clamp((int) (m * baseAxisLength * 16), 0, 15);

		Couple<Integer> smoothingResult = Couple.create(0, smoothingParam);
		Vec3 raisedOffset = diff.normalize()
			.add(0, Mth.clamp(m, 0, 1 - 1 / 512.0), 0)
			.normalize()
			.scale(baseAxisLength);

		highStarts.setFirst(lowStarts.getFirst()
			.add(raisedOffset));

		boolean first = true;
		for (BezierConnection bezierConnection : discoveredSlopes) {
			int smoothingToApply = smoothingResult.get(first);

			if (bezierConnection.smoothing == null)
				bezierConnection.smoothing = Couple.create(0, 0);
			bezierConnection.smoothing.setFirst(smoothingToApply);
			bezierConnection.axes.setFirst(bezierConnection.axes.getFirst()
				.add(0, (first ? 1 : -1) * -m, 0)
				.normalize());

			first = false;
			BlockPos otherPosition = bezierConnection.getKey();
			BlockState otherState = level.getBlockState(otherPosition);
			if (!(otherState.getBlock() instanceof TrackBlock))
				continue;
			level.setBlockAndUpdate(otherPosition, otherState.setValue(TrackBlock.HAS_BE, true));
			BlockEntity otherBE = level.getBlockEntity(otherPosition);
			if (otherBE instanceof TrackBlockEntity tbe) {
				blockEntity.addConnection(bezierConnection);
				tbe.addConnection(bezierConnection.secondary());
			}
		}
	}

	public void captureSmoothingHandles() {
		boolean first = true;
		previousSmoothingHandles = Couple.create(null, null);
		for (BezierConnection bezierConnection : blockEntity.connections.values()) {
			previousSmoothingHandles.set(first, Pair.of(bezierConnection.starts.getFirst(),
				bezierConnection.smoothing == null ? 0 : bezierConnection.smoothing.getFirst()));
			first = false;
		}
	}

	public void undoSmoothing() {
		if (smoothingAngle.isEmpty())
			return;
		if (previousSmoothingHandles == null)
			return;
		if (blockEntity.connections.size() == 2)
			return;

		BlockState blockState = blockEntity.getBlockState();
		BlockPos worldPosition = blockEntity.getBlockPos();
		Level level = blockEntity.getLevel();

		List<BezierConnection> validConnections = new ArrayList<>();
		for (BezierConnection bezierConnection : blockEntity.connections.values()) {
			BlockPos otherPosition = bezierConnection.getKey();
			BlockEntity otherBE = level.getBlockEntity(otherPosition);
			if (otherBE instanceof TrackBlockEntity tbe && tbe.connections.containsKey(worldPosition))
				validConnections.add(bezierConnection);
		}

		blockEntity.removeInboundConnections(false);
		TrackPropagator.onRailRemoved(level, worldPosition, blockState);
		blockEntity.connections.clear();
		smoothingAngle = Optional.empty();

		for (BezierConnection bezierConnection : validConnections) {
			blockEntity.addConnection(restoreToOriginalCurve(bezierConnection));

			BlockPos otherPosition = bezierConnection.getKey();
			BlockState otherState = level.getBlockState(otherPosition);
			if (!(otherState.getBlock() instanceof TrackBlock))
				continue;
			level.setBlockAndUpdate(otherPosition, otherState.setValue(TrackBlock.HAS_BE, true));
			BlockEntity otherBE = level.getBlockEntity(otherPosition);
			if (otherBE instanceof TrackBlockEntity tbe)
				tbe.addConnection(bezierConnection.secondary());
		}

		blockEntity.notifyUpdate();
		previousSmoothingHandles = null;
		TrackPropagator.onRailAdded(level, worldPosition, blockState);
	}

	public BezierConnection restoreToOriginalCurve(BezierConnection bezierConnection) {
		if (bezierConnection.smoothing != null) {
			bezierConnection.smoothing.setFirst(0);
			if (bezierConnection.smoothing.getFirst() == 0 && bezierConnection.smoothing.getSecond() == 0)
				bezierConnection.smoothing = null;
		}
		Vec3 raisedStart = bezierConnection.starts.getFirst();
		bezierConnection.starts.setFirst(new TrackNodeLocation(raisedStart).getLocation());
		bezierConnection.axes.setFirst(bezierConnection.axes.getFirst()
			.multiply(1, 0, 1)
			.normalize());
		return bezierConnection;
	}

	public int getYOffsetForAxisEnd(Vec3 end) {
		if (smoothingAngle.isEmpty())
			return 0;
		for (BezierConnection bezierConnection : blockEntity.connections.values())
			if (compareHandles(bezierConnection.starts.getFirst(), end))
				return bezierConnection.yOffsetAt(end);
		if (previousSmoothingHandles == null)
			return 0;
		for (Pair<Vec3, Integer> handle : previousSmoothingHandles)
			if (handle != null && compareHandles(handle.getFirst(), end))
				return handle.getSecond();
		return 0;
	}

	public static boolean compareHandles(Vec3 handle1, Vec3 handle2) {
		return new TrackNodeLocation(handle1).getLocation()
			.multiply(1, 0, 1)
			.distanceToSqr(new TrackNodeLocation(handle2).getLocation()
				.multiply(1, 0, 1)) < 1 / 512f;
	}

}
