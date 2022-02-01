package com.simibubi.create.content.logistics.trains.management;

import java.lang.ref.WeakReference;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

public class GlobalStation {

	public UUID id;
	public Couple<TrackNodeLocation> edgeLocation;
	public double position;
	public String name;
	public BlockPos stationPos;

	public WeakReference<Train> nearestTrain;

	public GlobalStation(UUID id, Couple<TrackNode> nodes, double position, BlockPos stationPos) {
		this.id = id;
		this.stationPos = stationPos;
		this.position = position;
		name = "Track Station";
		edgeLocation = nodes.map(TrackNode::getLocation);
		nearestTrain = new WeakReference<Train>(null);
	}

	public GlobalStation(CompoundTag nbt) {
		id = nbt.getUUID("Id");
		name = nbt.getString("Name");
		position = nbt.getDouble("Position");
		stationPos = NbtUtils.readBlockPos(nbt.getCompound("StationPos"));
		nearestTrain = new WeakReference<Train>(null);
		edgeLocation = Couple.deserializeEach(nbt.getList("Edge", Tag.TAG_COMPOUND),
			tag -> TrackNodeLocation.fromPackedPos(NbtUtils.readBlockPos(tag)));
	}

	public void reserveFor(Train train) {
		Train nearestTrain = this.nearestTrain.get();
		if (nearestTrain == null
			|| nearestTrain.navigation.distanceToDestination > train.navigation.distanceToDestination)
			this.nearestTrain = new WeakReference<>(train);
	}

	public void cancelReservation(Train train) {
		if (nearestTrain.get() == train)
			nearestTrain = new WeakReference<>(null);
	}

	public void trainDeparted(Train train) {
		cancelReservation(train);
	}
	
	@Nullable
	public Train getPresentTrain() {
		Train nearestTrain = this.nearestTrain.get();
		if (nearestTrain == null || nearestTrain.currentStation != this)
			return null;
		return nearestTrain;
	}

	@Nullable
	public Train getImminentTrain() {
		Train nearestTrain = this.nearestTrain.get();
		if (nearestTrain == null)
			return nearestTrain;
		if (nearestTrain.currentStation == this)
			return nearestTrain;
		if (!nearestTrain.navigation.isActive())
			return null;
		if (nearestTrain.navigation.distanceToDestination > 30)
			return null;
		return nearestTrain;
	}

	public CompoundTag write() {
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("Id", id);
		nbt.putString("Name", name);
		nbt.put("StationPos", NbtUtils.writeBlockPos(stationPos));
		nbt.putDouble("Position", position);
		nbt.put("Edge", edgeLocation.serializeEach(loc -> NbtUtils.writeBlockPos(new BlockPos(loc))));
		return nbt;
	}

}
