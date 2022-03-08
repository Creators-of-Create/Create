package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.nbt.CompoundTag;

public class SignalEdgeGroup {

	public UUID id;
	public Color color;

	public Set<Train> trains;
	public SignalBoundary reserved;

	public SignalEdgeGroup(UUID id) {
		this.id = id;
		color = Color.rainbowColor(Create.RANDOM.nextInt());
		trains = new HashSet<>();
	}

	public boolean isOccupiedUnless(Train train) {
		return reserved != null || trains.size() > 1 || !trains.contains(train) && !trains.isEmpty();
	}

	public boolean isOccupiedUnless(SignalBoundary boundary) {
		return !trains.isEmpty() || reserved != null && reserved != boundary;
	}

	public boolean isOccupied() {
		return !trains.isEmpty() || reserved != null;
	}

	public static SignalEdgeGroup read(CompoundTag tag) {
		SignalEdgeGroup group = new SignalEdgeGroup(tag.getUUID("Id"));
		group.color = new Color(tag.getInt("Color"));
		return group;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("Id", id);
		tag.putInt("Color", color.getRGB());
		return tag;
	}

}
