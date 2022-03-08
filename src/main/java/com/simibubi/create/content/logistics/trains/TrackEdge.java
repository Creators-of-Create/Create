package com.simibubi.create.content.logistics.trains;

import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgeData;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TrackEdge {

	BezierConnection turn;
	EdgeData edgeData;

	public TrackEdge(BezierConnection turn) {
		this.turn = turn;
		this.edgeData = new EdgeData();
	}

	public boolean isTurn() {
		return turn != null;
	}

	public EdgeData getEdgeData() {
		return edgeData;
	}

	public BezierConnection getTurn() {
		return turn;
	}

	public Vec3 getDirection(TrackNode node1, TrackNode node2, boolean fromFirst) {
		return getPosition(node1, node2, fromFirst ? 0.25f : 1)
			.subtract(getPosition(node1, node2, fromFirst ? 0 : 0.75f))
			.normalize();
	}

	public double getLength(TrackNode node1, TrackNode node2) {
		return isTurn() ? turn.getLength()
			: node1.location.getLocation()
				.distanceTo(node2.location.getLocation());
	}

	public double incrementT(TrackNode node1, TrackNode node2, double currentT, double distance) {
		distance = distance / getLength(node1, node2);
		return isTurn() ? turn.incrementT(currentT, distance) : currentT + distance;
	}

	public Vec3 getPosition(TrackNode node1, TrackNode node2, double t) {
		return isTurn() ? turn.getPosition(Mth.clamp(t, 0, 1))
			: VecHelper.lerp((float) t, node1.location.getLocation(), node2.location.getLocation());
	}

	public Vec3 getNormal(TrackNode node1, TrackNode node2, double t) {
		return isTurn() ? turn.getNormal(Mth.clamp(t, 0, 1)) : node1.getNormal();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(isTurn());
		if (isTurn())
			turn.write(buffer);
	}

	public static TrackEdge read(FriendlyByteBuf buffer) {
		return new TrackEdge(buffer.readBoolean() ? new BezierConnection(buffer) : null);
	}

	public CompoundTag write() {
		CompoundTag baseCompound = isTurn() ? turn.write(BlockPos.ZERO) : new CompoundTag();
		baseCompound.put("Signals", edgeData.write());
		return baseCompound;
	}

	public static TrackEdge read(CompoundTag tag, TrackGraph graph) {
		TrackEdge trackEdge =
			new TrackEdge(tag.contains("Positions") ? new BezierConnection(tag, BlockPos.ZERO) : null);
		trackEdge.edgeData = EdgeData.read(tag.getCompound("Signals"), graph);
		return trackEdge;
	}

}
