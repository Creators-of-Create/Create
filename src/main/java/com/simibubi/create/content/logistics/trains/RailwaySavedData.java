package com.simibubi.create.content.logistics.trains;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroup;
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
		Create.LOGGER.info("Saving Railway Information...");
		nbt.put("RailGraphs", NBTHelper.writeCompoundList(railways.trackNetworks.values(), TrackGraph::write));
		nbt.put("SignalBlocks",
			NBTHelper.writeCompoundList(railways.signalEdgeGroups.values(), SignalEdgeGroup::write));
		nbt.put("Trains", NBTHelper.writeCompoundList(railways.trains.values(), Train::write));
		return nbt;
	}

	private static RailwaySavedData load(CompoundTag nbt) {
		RailwaySavedData sd = new RailwaySavedData();
		sd.trackNetworks = new HashMap<>();
		sd.signalEdgeGroups = new HashMap<>();
		sd.trains = new HashMap<>();
		Create.LOGGER.info("Loading Railway Information...");
		NBTHelper.iterateCompoundList(nbt.getList("RailGraphs", Tag.TAG_COMPOUND), c -> {
			TrackGraph graph = TrackGraph.read(c);
			sd.trackNetworks.put(graph.id, graph);
		});
		NBTHelper.iterateCompoundList(nbt.getList("Trains", Tag.TAG_COMPOUND), c -> {
			Train train = Train.read(c, sd.trackNetworks);
			sd.trains.put(train.id, train);
		});
		NBTHelper.iterateCompoundList(nbt.getList("SignalBlocks", Tag.TAG_COMPOUND), c -> {
			SignalEdgeGroup group = SignalEdgeGroup.read(c);
			sd.signalEdgeGroups.put(group.id, group);
		});
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
