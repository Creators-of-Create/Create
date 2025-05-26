package com.simibubi.create.content.trains.graph;

import java.util.List;

import com.simibubi.create.content.trains.entity.Destination;
import com.simibubi.create.content.trains.platform.GlobalPlatform;

import net.createmod.catnip.data.Couple;

public class DiscoveredPath {
	public List<Couple<TrackNode>> path;
	public Destination destination;
	public double distance;
	public double cost;

	public DiscoveredPath(double distance, double cost, List<Couple<TrackNode>> path, Destination destination) {
		this.distance = distance;
		this.cost = cost;
		this.path = path;
		this.destination = destination;
	}
}
