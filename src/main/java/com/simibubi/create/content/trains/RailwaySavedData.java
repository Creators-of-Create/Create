package com.simibubi.create.content.trains;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.edgePoint.EdgePointType;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class RailwaySavedData extends SavedData {

	private Map<UUID, TrackGraph> trackNetworks = new HashMap<>();
	private Map<UUID, SignalEdgeGroup> signalEdgeGroups = new HashMap<>();
	private Map<UUID, Train> trains = new HashMap<>();

	@Override
	public CompoundTag save(CompoundTag nbt) {
		GlobalRailwayManager railways = Create.RAILWAYS;
//		Create.LOGGER.info("Saving Railway Information...");
		DimensionPalette dimensions = new DimensionPalette();
		nbt.put("RailGraphs", NBTHelper.writeCompoundList(railways.trackNetworks.values(), tg -> tg.write(dimensions)));
		nbt.put("SignalBlocks", NBTHelper.writeCompoundList(railways.signalEdgeGroups.values(), seg -> {
			if (seg.fallbackGroup && !railways.trackNetworks.containsKey(seg.id))
				return null;
			return seg.write();
		}));
		nbt.put("Trains", NBTHelper.writeCompoundList(railways.trains.values(), t -> t.write(dimensions)));
		dimensions.write(nbt);
		return nbt;
	}

	private static RailwaySavedData load(CompoundTag nbt) {
		RailwaySavedData sd = new RailwaySavedData();
		sd.trackNetworks = new HashMap<>();
		sd.signalEdgeGroups = new HashMap<>();
		sd.trains = new HashMap<>();
//		Create.LOGGER.info("Loading Railway Information...");

		DimensionPalette dimensions = DimensionPalette.read(nbt);
		NBTHelper.iterateCompoundList(nbt.getList("RailGraphs", Tag.TAG_COMPOUND), c -> {
			TrackGraph graph = TrackGraph.read(c, dimensions);
			sd.trackNetworks.put(graph.id, graph);
		});
		NBTHelper.iterateCompoundList(nbt.getList("SignalBlocks", Tag.TAG_COMPOUND), c -> {
			SignalEdgeGroup group = SignalEdgeGroup.read(c);
			sd.signalEdgeGroups.put(group.id, group);
		});
		NBTHelper.iterateCompoundList(nbt.getList("Trains", Tag.TAG_COMPOUND), c -> {
			Train train = Train.read(c, sd.trackNetworks, dimensions);
			sd.trains.put(train.id, train);
		});

		for (TrackGraph graph : sd.trackNetworks.values()) {
			for (SignalBoundary signal : graph.getPoints(EdgePointType.SIGNAL)) {
				UUID groupId = signal.groups.getFirst();
				UUID otherGroupId = signal.groups.getSecond();
				if (groupId == null || otherGroupId == null)
					continue;
				SignalEdgeGroup group = sd.signalEdgeGroups.get(groupId);
				SignalEdgeGroup otherGroup = sd.signalEdgeGroups.get(otherGroupId);
				if (group == null || otherGroup == null)
					continue;
				group.putAdjacent(otherGroupId);
				otherGroup.putAdjacent(groupId);
			}
		}

		return sd;
	}

	public Map<UUID, TrackGraph> getTrackNetworks() {
		return trackNetworks;
	}

	public Map<UUID, Train> getTrains() {
		return trains;
	}

	public Map<UUID, SignalEdgeGroup> getSignalBlocks() {
		return signalEdgeGroups;
	}

	private RailwaySavedData() {}

	public static RailwaySavedData load(MinecraftServer server) {
		return server.overworld()
			.getDataStorage()
			.computeIfAbsent(RailwaySavedData::load, RailwaySavedData::new, "create_tracks");
	}

}
