package com.simibubi.create.content.logistics.trains.management.signal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

public class EdgeData {

	public UUID singleSignalGroup;
	private List<SignalBoundary> boundaries;
	private List<GlobalStation> stations;

	public EdgeData() {
		boundaries = new ArrayList<>();
		stations = new ArrayList<>();
		singleSignalGroup = null;
	}

	public boolean hasBoundaries() {
		return !boundaries.isEmpty();
	}

	public boolean hasStations() {
		return !stations.isEmpty();
	}

	public List<SignalBoundary> getBoundaries() {
		return boundaries;
	}

	public List<GlobalStation> getStations() {
		return stations;
	}

	public void removePoint(TrackNode node1, TrackNode node2, TrackEdge edge, TrackEdgePoint point) {
		if (point instanceof GlobalStation gs)
			stations.remove(gs);
		if (point instanceof SignalBoundary sb)
			boundaries.remove(sb);
		updateDelegates(node1, node2, edge);
	}

	public <T extends TrackEdgePoint> void addPoint(TrackNode node1, TrackNode node2, TrackEdge edge, T boundary,
		Class<T> type) {
		T next = next(type, node1, node2, edge, boundary.getLocationOn(node1, node2, edge));
		if (boundary instanceof GlobalStation gs)
			stations.add(next == null ? stations.size() : stations.indexOf(next), gs);
		if (boundary instanceof SignalBoundary sb)
			boundaries.add(next == null ? boundaries.size() : boundaries.indexOf(next), sb);
		updateDelegates(node1, node2, edge);
	}

	public void updateDelegates(TrackNode node1, TrackNode node2, TrackEdge edge) {
		for (GlobalStation globalStation : stations)
			globalStation.boundary = getBoundary(node1, node2, edge, globalStation.getLocationOn(node1, node2, edge));
		for (SignalBoundary boundary : boundaries)
			boundary.station = getStation(node1, node2, edge, boundary.getLocationOn(node1, node2, edge));
	}

	@Nullable
	public SignalBoundary nextBoundary(TrackNode node1, TrackNode node2, TrackEdge edge, double minPosition) {
		return next(SignalBoundary.class, node1, node2, edge, minPosition);
	}

	@Nullable
	public GlobalStation nextStation(TrackNode node1, TrackNode node2, TrackEdge edge, double minPosition) {
		return next(GlobalStation.class, node1, node2, edge, minPosition);
	}

	@Nullable
	public SignalBoundary getBoundary(TrackNode node1, TrackNode node2, TrackEdge edge, double exactPosition) {
		return get(SignalBoundary.class, node1, node2, edge, exactPosition);
	}

	@Nullable
	public GlobalStation getStation(TrackNode node1, TrackNode node2, TrackEdge edge, double exactPosition) {
		return get(GlobalStation.class, node1, node2, edge, exactPosition);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T extends TrackEdgePoint> T next(Class<T> type, TrackNode node1, TrackNode node2, TrackEdge edge,
		double minPosition) {
		for (TrackEdgePoint point : type == GlobalStation.class ? stations : boundaries)
			if (point.getLocationOn(node1, node2, edge) > minPosition)
				return (T) point;
		return null;
	}

	@Nullable
	private <T extends TrackEdgePoint> T get(Class<T> type, TrackNode node1, TrackNode node2, TrackEdge edge,
		double exactPosition) {
		T next = next(type, node1, node2, edge, exactPosition - .5f);
		if (next != null && Mth.equal(next.getLocationOn(node1, node2, edge), exactPosition))
			return next;
		return null;
	}

	public CompoundTag write() {
		CompoundTag signalCompound = new CompoundTag();
		if (hasBoundaries()) {
			signalCompound.put("Boundaries", NBTHelper.writeCompoundList(boundaries, this::writePoint));
		} else if (singleSignalGroup != null)
			signalCompound.putUUID("Group", singleSignalGroup);
		if (hasStations())
			signalCompound.put("Stations", NBTHelper.writeCompoundList(stations, this::writePoint));
		return signalCompound;
	}

	public static EdgeData read(CompoundTag tag, TrackGraph graph) {
		EdgeData signalEdgeData = new EdgeData();
		if (tag.contains("Group"))
			signalEdgeData.singleSignalGroup = tag.getUUID("Group");
		if (tag.contains("Boundaries"))
			NBTHelper.iterateCompoundList(tag.getList("Boundaries", Tag.TAG_COMPOUND),
				readPoint(graph::getSignal, signalEdgeData.boundaries));
		if (tag.contains("Stations"))
			NBTHelper.iterateCompoundList(tag.getList("Stations", Tag.TAG_COMPOUND),
				readPoint(graph::getStation, signalEdgeData.stations));
		return signalEdgeData;
	}

	private <T extends TrackEdgePoint> CompoundTag writePoint(T point) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putUUID("Id", point.id);
		return compoundTag;
	}

	private static <T extends TrackEdgePoint> Consumer<CompoundTag> readPoint(Function<UUID, T> lookup,
		Collection<T> target) {
		return tag -> {
			UUID id = tag.getUUID("Id");
			T signal = lookup.apply(id);
			if (signal != null)
				target.add(signal);
		};
	}

}
