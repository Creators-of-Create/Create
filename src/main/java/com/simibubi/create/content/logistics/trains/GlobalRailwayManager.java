package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.trains.entity.Carriage;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.signal.SignalEdgeGroup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public class GlobalRailwayManager {

	public Map<UUID, TrackGraph> trackNetworks;
	public Map<UUID, SignalEdgeGroup> signalEdgeGroups;
	public Map<UUID, Train> trains;
	public Map<Integer, Carriage> carriageById;

	private TrackSavedData trackData;
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
			sync.sendEdgeGroups(signalEdgeGroups.keySet(), serverPlayer);
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
		signalEdgeGroups = trackData.getSignalBlocks();
	}

	public void levelUnloaded(LevelAccessor level) {
//		MinecraftServer server = level.getServer();
//		if (server == null || server.overworld() != level)
//			return;
//		cleanUp();
	}

	public void cleanUp() {
		trackNetworks = new HashMap<>();
		signalEdgeGroups = new HashMap<>();
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

	public List<TrackGraph> getGraphs(LevelAccessor level, TrackNodeLocation vertex) {
		if (trackNetworks == null)
			return Collections.emptyList();
		ArrayList<TrackGraph> intersecting = new ArrayList<>();
		for (TrackGraph railGraph : trackNetworks.values())
			if (railGraph.locateNode(vertex) != null)
				intersecting.add(railGraph);
		return intersecting;
	}

	public void tick(Level level) {
		ResourceLocation location2 = DimensionType.OVERWORLD_LOCATION.location();
		ResourceLocation location = level.dimension()
			.location();
		if (!location.equals(location2))
			return;

		for (SignalEdgeGroup group : signalEdgeGroups.values()) {
			group.trains.clear();
			group.reserved = null;
		}

		for (TrackGraph graph : trackNetworks.values())
			graph.tickPoints();
		for (Train train : trains.values())
			train.earlyTick(level);
		for (Train train : trains.values())
			train.tick(level);

		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_H) && AllKeys.altDown())
			trackNetworks.values()
				.forEach(TrackGraph::debugViewSignalData);
		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_J) && AllKeys.altDown())
			trackNetworks.values()
				.forEach(TrackGraph::debugViewNodes);
	}

	public void clientTick() {
		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_H) && !AllKeys.altDown())
			trackNetworks.values()
				.forEach(TrackGraph::debugViewSignalData);
		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_J) && !AllKeys.altDown())
			trackNetworks.values()
				.forEach(TrackGraph::debugViewNodes);
	}

}
