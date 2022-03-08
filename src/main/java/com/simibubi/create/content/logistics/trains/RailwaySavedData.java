package com.simibubi.create.content.logistics.trains;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroup;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class RailwaySavedData extends SavedData {

	private Map<UUID, TrackGraph> trackNetworks = new HashMap<>();
	private Map<UUID, SignalEdgeGroup> signalEdgeGroups = new HashMap<>();

	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt.put("RailGraphs", NBTHelper.writeCompoundList(Create.RAILWAYS.trackNetworks.values(), TrackGraph::write));
		nbt.put("SignalBlocks",
			NBTHelper.writeCompoundList(Create.RAILWAYS.signalEdgeGroups.values(), SignalEdgeGroup::write));
		return nbt;
	}

	private static RailwaySavedData load(CompoundTag nbt) {
		RailwaySavedData sd = new RailwaySavedData();//TODO load trains before everything else
		sd.trackNetworks = new HashMap<>();
		sd.signalEdgeGroups = new HashMap<>();
		NBTHelper.iterateCompoundList(nbt.getList("RailGraphs", Tag.TAG_COMPOUND), c -> {
			TrackGraph graph = TrackGraph.read(c);
			sd.trackNetworks.put(graph.id, graph);
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
