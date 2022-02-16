package com.simibubi.create.content.logistics.trains.management.signal;

import java.util.UUID;

import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

public class TrackEdgePoint {

	public UUID id;
	public Couple<TrackNodeLocation> edgeLocation;
	public double position;

	public TrackEdgePoint(UUID id) {
		this.id = id;
	}
	
	public TrackEdgePoint(CompoundTag nbt) {
		id = nbt.getUUID("Id");
		position = nbt.getDouble("Position");
		edgeLocation = Couple.deserializeEach(nbt.getList("Edge", Tag.TAG_COMPOUND),
			tag -> TrackNodeLocation.fromPackedPos(NbtUtils.readBlockPos(tag)));
	}

	public void setLocation(Couple<TrackNodeLocation> nodes, double position) {
		this.edgeLocation = nodes;
		this.position = position;
	}

	public double getLocationOn(TrackNode node1, TrackNode node2, TrackEdge edge) {
		return isPrimary(node1) ? edge.getLength(node1, node2) - position : position;
	}

	public boolean isPrimary(TrackNode node1) {
		return edgeLocation.getSecond()
			.equals(node1.getLocation());
	}

}
