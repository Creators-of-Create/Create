package com.simibubi.create.api.event;

import com.simibubi.create.content.trains.graph.TrackGraph;

import net.neoforged.bus.api.Event;

public class TrackGraphMergeEvent extends Event {
	private final TrackGraph mergedInto;
	private final TrackGraph mergedFrom;
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
