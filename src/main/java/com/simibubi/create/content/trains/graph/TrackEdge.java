package com.simibubi.create.content.trains.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackEdge {

	public TrackNode node1;
	public TrackNode node2;
	BezierConnection turn;
	EdgeData edgeData;
	boolean interDimensional;
	TrackMaterial trackMaterial;

	public TrackEdge(TrackNode node1, TrackNode node2, BezierConnection turn, TrackMaterial trackMaterial) {
		this.interDimensional = !node1.location.dimension.equals(node2.location.dimension);
		this.edgeData = new EdgeData(this);
		this.node1 = node1;
		this.node2 = node2;
		this.turn = turn;
		this.trackMaterial = trackMaterial;
	}

	public TrackMaterial getTrackMaterial() {
		return trackMaterial;
	}

	public boolean isTurn() {
		return turn != null;
	}

	public boolean isInterDimensional() {
		return interDimensional;
	}

	public EdgeData getEdgeData() {
		return edgeData;
	}

	public BezierConnection getTurn() {
		return turn;
	}

	public Vec3 getDirection(boolean fromFirst) {
		return getPosition(null, fromFirst ? 0.25f : 1).subtract(getPosition(null, fromFirst ? 0 : 0.75f))
				.normalize();
	}

	public Vec3 getDirectionAt(double t) {
		double length = getLength();
		double step = .5f / length;
		t /= length;
		Vec3 ahead = getPosition(null, Math.min(1, t + step));
		Vec3 behind = getPosition(null, Math.max(0, t - step));
		return ahead.subtract(behind)
				.normalize();
	}

	public boolean canTravelTo(TrackEdge other) {
		if (isInterDimensional() || other.isInterDimensional())
			return true;
		Vec3 newDirection = other.getDirection(true);
		boolean result = (getDirection(false).dot(newDirection) > 7 / 8f);
		if(Mods.JSG.isLoaded())
			return true;
		return result;
	}

	public double getLength() {
		return isInterDimensional() ? 0
				: isTurn() ? turn.getLength()
				: node1.location.getLocation()
				.distanceTo(node2.location.getLocation());
	}

	public double incrementT(double currentT, double distance) {
		boolean tooFar = Math.abs(distance) > 5;
		double length = getLength();
		distance = distance / (length == 0 ? 1 : length);
		return !tooFar && isTurn() ? turn.incrementT(currentT, distance) : currentT + distance;
	}

	public Vec3 getPosition(@Nullable TrackGraph trackGraph, double t) {
		if (isTurn())
			return turn.getPosition(Mth.clamp(t, 0, 1));
		if (trackGraph != null && (node1.location.yOffsetPixels != 0 || node2.location.yOffsetPixels != 0)) {
			Vec3 positionSmoothed = getPositionSmoothed(trackGraph, t);
			if (positionSmoothed != null)
				return positionSmoothed;
		}
		return VecHelper.lerp((float) t, node1.location.getLocation(), node2.location.getLocation());
	}

	public Vec3 getNormal(@Nullable TrackGraph trackGraph, double t) {
		if (isTurn())
			return turn.getNormal(Mth.clamp(t, 0, 1));
		if (trackGraph != null && (node1.location.yOffsetPixels != 0 || node2.location.yOffsetPixels != 0)) {
			Vec3 normalSmoothed = getNormalSmoothed(trackGraph, t);
			if (normalSmoothed != null)
				return normalSmoothed;
		}
		return node1.getNormal();
	}

	@Nullable
	public Vec3 getPositionSmoothed(TrackGraph trackGraph, double t) {
		Vec3 node1Location = null;
		Vec3 node2Location = null;
		for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node1)
				.values())
			if (trackEdge.isTurn())
				node1Location = trackEdge.getPosition(trackGraph, 0);
		for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node2)
				.values())
			if (trackEdge.isTurn())
				node2Location = trackEdge.getPosition(trackGraph, 0);
		if (node1Location == null || node2Location == null)
			return null;
		return VecHelper.lerp((float) t, node1Location, node2Location);
	}

	@Nullable
	public Vec3 getNormalSmoothed(TrackGraph trackGraph, double t) {
		Vec3 node1Normal = null;
		Vec3 node2Normal = null;
		for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node1)
				.values())
			if (trackEdge.isTurn())
				node1Normal = trackEdge.getNormal(trackGraph, 0);
		for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node2)
				.values())
			if (trackEdge.isTurn())
				node2Normal = trackEdge.getNormal(trackGraph, 0);
		if (node1Normal == null || node2Normal == null)
			return null;
		return VecHelper.lerp(0.5f, node1Normal, node2Normal);
	}

	public Collection<double[]> getIntersection(TrackNode node1, TrackNode node2, TrackEdge other, TrackNode other1,
												TrackNode other2) {
		Vec3 v1 = node1.location.getLocation();
		Vec3 v2 = node2.location.getLocation();
		Vec3 w1 = other1.location.getLocation();
		Vec3 w2 = other2.location.getLocation();

		if (isInterDimensional() || other.isInterDimensional())
			return Collections.emptyList();
		if (v1.y != v2.y || v1.y != w1.y || v1.y != w2.y)
			return Collections.emptyList();

		if (!isTurn()) {
			if (!other.isTurn())
				return ImmutableList.of(VecHelper.intersectRanged(v1, w1, v2, w2, Axis.Y));
			return other.getIntersection(other1, other2, this, node1, node2)
					.stream()
					.map(a -> new double[]{a[1], a[0]})
					.toList();
		}

		AABB bb = turn.getBounds();

		if (!other.isTurn()) {
			if (!bb.intersects(w1, w2))
				return Collections.emptyList();

			Vec3 seg1 = v1;
			Vec3 seg2 = null;
			double t = 0;

			Collection<double[]> intersections = new ArrayList<>();
			for (int i = 0; i < turn.getSegmentCount(); i++) {
				double tOffset = t;
				t += .5;
				seg2 = getPosition(null, t / getLength());
				double[] intersection = VecHelper.intersectRanged(seg1, w1, seg2, w2, Axis.Y);
				seg1 = seg2;
				if (intersection == null)
					continue;
				intersection[0] += tOffset;
				intersections.add(intersection);
			}

			return intersections;
		}

		if (!bb.intersects(other.turn.getBounds()))
			return Collections.emptyList();

		Vec3 seg1 = v1;
		Vec3 seg2 = null;
		double t = 0;

		Collection<double[]> intersections = new ArrayList<>();
		for (int i = 0; i < turn.getSegmentCount(); i++) {
			double tOffset = t;
			t += .5;
			seg2 = getPosition(null, t / getLength());

			Vec3 otherSeg1 = w1;
			Vec3 otherSeg2 = null;
			double u = 0;

			for (int j = 0; j < other.turn.getSegmentCount(); j++) {
				double uOffset = u;
				u += .5;
				otherSeg2 = other.getPosition(null, u / other.getLength());

				double[] intersection = VecHelper.intersectRanged(seg1, otherSeg1, seg2, otherSeg2, Axis.Y);
				otherSeg1 = otherSeg2;

				if (intersection == null)
					continue;

				intersection[0] += tOffset;
				intersection[1] += uOffset;
				intersections.add(intersection);
			}

			seg1 = seg2;
		}

		return intersections;
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag baseCompound = isTurn() ? turn.write(BlockPos.ZERO) : new CompoundTag();
		baseCompound.put("Signals", edgeData.write(dimensions));
		baseCompound.putString("Material", getTrackMaterial().id.toString());
		return baseCompound;
	}

	public static TrackEdge read(TrackNode node1, TrackNode node2, CompoundTag tag, TrackGraph graph,
								 DimensionPalette dimensions) {
		TrackEdge trackEdge =
				new TrackEdge(node1, node2, tag.contains("Positions") ? new BezierConnection(tag, BlockPos.ZERO) : null,
						TrackMaterial.deserialize(tag.getString("Material")));
		trackEdge.edgeData = EdgeData.read(tag.getCompound("Signals"), trackEdge, graph, dimensions);
		return trackEdge;
	}

}
