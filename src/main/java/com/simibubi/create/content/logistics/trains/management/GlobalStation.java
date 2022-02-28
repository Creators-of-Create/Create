package com.simibubi.create.content.logistics.trains.management;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.signal.SingleTileEdgePoint;

import net.minecraft.nbt.CompoundTag;

public class GlobalStation extends SingleTileEdgePoint {

	public String name;
	public WeakReference<Train> nearestTrain;

	public GlobalStation() {
		name = "Track Station";
		nearestTrain = new WeakReference<Train>(null);
	}

	@Override
	public void read(CompoundTag nbt, boolean migration) {
		super.read(nbt, migration);
		name = nbt.getString("Name");
		nearestTrain = new WeakReference<Train>(null);
	}

	@Override
	public void write(CompoundTag nbt) {
		super.write(nbt);
		nbt.putString("Name", name);
	}
	
	public boolean canApproachFrom(TrackNode side) {
		return isPrimary(side);
	}

	public void reserveFor(Train train) {
		Train nearestTrain = getNearestTrain();
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
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null || nearestTrain.getCurrentStation() != this)
			return null;
		return nearestTrain;
	}

	@Nullable
	public Train getImminentTrain() {
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null)
			return nearestTrain;
		if (nearestTrain.getCurrentStation() == this)
			return nearestTrain;
		if (!nearestTrain.navigation.isActive())
			return null;
		if (nearestTrain.navigation.distanceToDestination > 30)
			return null;
		return nearestTrain;
	}

	@Nullable
	public Train getNearestTrain() {
		return this.nearestTrain.get();
	}

}
