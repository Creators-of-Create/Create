package com.simibubi.create.content.trains.station;

import com.simibubi.create.content.trains.entity.Destination;
import com.simibubi.create.content.trains.entity.Train;

import com.simibubi.create.content.trains.entry.GlobalEntry;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.platform.GlobalPlatform;

import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import com.simibubi.create.content.trains.waypoint.GlobalWaypoint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.ArrayList;

public class GlobalStation extends SingleBlockEntityEdgePoint {
	public ArrayList<GlobalPlatform> PLATFORMS = new ArrayList<>();
	public ArrayList<GlobalEntry> ENTRIES = new ArrayList<>();
	public ArrayList<GlobalWaypoint> THROUGH = new ArrayList<>();

	public ArrayList<Destination> PLATFORM_DEST = new ArrayList<>();
	public ArrayList<Destination> ENTRY_DEST = new ArrayList<>();
	public ArrayList<Destination> THROUGH_DEST = new ArrayList<>();

	public ArrayList<Train> ALL_TRAINS = new ArrayList<>();
	public ArrayList<Train> IMMINENT_TRAINS = new ArrayList<>();
	public ArrayList<Train> ARRIVED_TRAINS = new ArrayList<>();

	public boolean allow_through;


	public GlobalStation addPlatform(GlobalPlatform platform) {
		this.PLATFORMS.add(platform);
		return this;
	}

	public GlobalStation addEntry(GlobalEntry entry) {
		this.ENTRIES.add(entry);
		return this;
	}

	public GlobalStation addThrough(GlobalWaypoint through) {
		this.THROUGH.add(through);
		return this;
	}

	public void addImminentTrain(Train train) {
		IMMINENT_TRAINS.add(train);
	}

	public void removeImminentTrain(Train train) {
		IMMINENT_TRAINS.remove(train);
	}

	public void routeTrain(Train train, TrackNode location) {
		if (train.navigation.destination.STATION != this) {
			if (this.allow_through) {
				routeTrainToThrough(train);
			}
			return;
		}

		routeTrainToPlatform(train, location);
	}

	public void routeTrainToPlatform(Train train, TrackNode location) {
		float distance = Float.MAX_VALUE;
		BlockPos nearestPlatform = null;

		for (GlobalPlatform platform : PLATFORMS) {
			if (platform.isReserved())
				continue;

			if (platform.canNavigateVia(location))
				continue;
		}
	}

	public void routeTrainToThrough(Train train) {
		DiscoveredPath bestThrough = train.navigation.findPathTo(THROUGH);
		train.navigation.startNavigation(bestThrough);
	}
}
