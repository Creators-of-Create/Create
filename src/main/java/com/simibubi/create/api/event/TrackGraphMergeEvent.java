package com.simibubi.create.api.event;

import com.simibubi.create.content.logistics.trains.TrackGraph;

import net.minecraftforge.eventbus.api.Event;

public class TrackGraphMergeEvent extends Event{
	private TrackGraph mergedInto, mergedFrom;
	
	public TrackGraphMergeEvent(TrackGraph from, TrackGraph into) {
		mergedInto = into;
		mergedFrom = from;
	}
	
	public TrackGraph getGraphMergedInto() {
		return mergedInto;
	}
	
	public TrackGraph getGraphMergedFrom() {
		return mergedFrom;
	}
}
