package com.simibubi.create.content.logistics.trains;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class TrackSavedData extends SavedData {

	private Map<UUID, TrackGraph> trackNetworks = new HashMap<>();

	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt.put("RailGraphs", NBTHelper.writeCompoundList(Create.RAILWAYS.trackNetworks.values(), TrackGraph::write));
		return nbt;
	}

	private static TrackSavedData load(CompoundTag nbt) {
		TrackSavedData sd = new TrackSavedData();
		sd.trackNetworks = new HashMap<>();
		NBTHelper.iterateCompoundList(nbt.getList("RailGraphs", Tag.TAG_COMPOUND), c -> {
			TrackGraph graph = TrackGraph.read(c);
			sd.trackNetworks.put(graph.id, graph);
		});
		return sd;
	}

	public Map<UUID, TrackGraph> getTrackNetworks() {
		return trackNetworks;
	}

	private TrackSavedData() {}

	public static TrackSavedData load(MinecraftServer server) {
		return server.overworld()
			.getDataStorage()
			.computeIfAbsent(TrackSavedData::load, TrackSavedData::new, "create_tracks");
	}

}
