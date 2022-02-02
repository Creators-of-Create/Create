package com.simibubi.create.content.logistics.trains;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.logistics.trains.entity.Carriage;
import com.simibubi.create.content.logistics.trains.entity.Train;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public class GlobalRailwayManager {

	public Map<UUID, TrackGraph> trackNetworks;
	private TrackSavedData trackData;

	public Map<UUID, Train> trains;
	public Map<Integer, Carriage> carriageById;
	
	public TrackGraphSync sync;

	//

	public GlobalRailwayManager() {
		cleanUp();
	}

	public void playerLogin(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			loadTrackData(serverPlayer.getServer());
			trackNetworks.values()
				.forEach(g -> sync.sendFullGraphTo(g, serverPlayer));
		}
	}

	public void levelLoaded(LevelAccessor level) {
		MinecraftServer server = level.getServer();
		if (server == null || server.overworld() != level)
			return;
		cleanUp();
		trackData = null;
		loadTrackData(server);
	}

	private void loadTrackData(MinecraftServer server) {
		if (trackData != null)
			return;
		trackData = TrackSavedData.load(server);
		trackNetworks = trackData.getTrackNetworks();
	}

	public void levelUnloaded(LevelAccessor level) {
//		MinecraftServer server = level.getServer();
//		if (server == null || server.overworld() != level)
//			return;
//		cleanUp();
	}

	public void cleanUp() {
		trackNetworks = new HashMap<>();
		trains = new HashMap<>();
		carriageById = new HashMap<>();
		sync = new TrackGraphSync();
	}

	public void markTracksDirty() {
		if (trackData != null)
			trackData.setDirty();
	}
	
	//

	public TrackGraph getOrCreateGraph(UUID graphID) {
		return trackNetworks.computeIfAbsent(graphID, uid -> new TrackGraph(graphID));
	}

	public void putGraph(TrackGraph graph) {
		trackNetworks.put(graph.id, graph);
		markTracksDirty();
	}

	public void removeGraph(TrackGraph railGraph) {
		trackNetworks.remove(railGraph.id);
		markTracksDirty();
	}

	public void updateSplitGraph(TrackGraph graph) {
		Set<TrackGraph> disconnected = graph.findDisconnectedGraphs(null);
		disconnected.forEach(this::putGraph);
		if (!disconnected.isEmpty()) {
			sync.graphSplit(graph, disconnected);
			markTracksDirty();
		}
	}

	@Nullable
	public TrackGraph getGraph(LevelAccessor level, TrackNodeLocation vertex) {
		if (trackNetworks == null)
			return null;
		for (TrackGraph railGraph : trackNetworks.values())
			if (railGraph.locateNode(vertex) != null)
				return railGraph;
		return null;
	}

	public void tick(Level level) {
		ResourceLocation location2 = DimensionType.OVERWORLD_LOCATION.location();
		ResourceLocation location = level.dimension()
			.location();
		if (!location.equals(location2))
			return;

		for (Train train : trains.values())
			train.tick(level);
		
		
	}

	public void clientTick() {
		if (KineticDebugger.isActive()) {
			trackNetworks.values()
			.forEach(TrackGraph::debugViewNodes);
		}
	}

}
