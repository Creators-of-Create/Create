package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableObject;
import org.lwjgl.glfw.GLFW;

import com.simibubi.create.AllKeys;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.entity.TrainPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroup;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;

public class GlobalRailwayManager {

	public Map<UUID, TrackGraph> trackNetworks;
	public Map<UUID, SignalEdgeGroup> signalEdgeGroups;
	public Map<UUID, Train> trains;
	public TrackGraphSync sync;

	private List<Train> movingTrains;
	private List<Train> waitingTrains;

	private RailwaySavedData savedData;

	public GlobalRailwayManager() {
		cleanUp();
	}

	public void playerLogin(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			loadTrackData(serverPlayer.getServer());
			trackNetworks.values()
				.forEach(g -> sync.sendFullGraphTo(g, serverPlayer));
			sync.sendEdgeGroups(signalEdgeGroups.keySet(), serverPlayer);
			for (Train train : trains.values())
				AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
					new TrainPacket(train, true));
		}
	}

	public void playerLogout(Player player) {}

	public void levelLoaded(LevelAccessor level) {
		MinecraftServer server = level.getServer();
		if (server == null || server.overworld() != level)
			return;
		cleanUp();
		savedData = null;
		loadTrackData(server);
	}

	private void loadTrackData(MinecraftServer server) {
		if (savedData != null)
			return;
		savedData = RailwaySavedData.load(server);
		trains = savedData.getTrains();
		trackNetworks = savedData.getTrackNetworks();
		signalEdgeGroups = savedData.getSignalBlocks();
		trains.values()
			.forEach(movingTrains::add);
	}

	public void cleanUp() {
		trackNetworks = new HashMap<>();
		signalEdgeGroups = new HashMap<>();
		trains = new HashMap<>();
		sync = new TrackGraphSync();
		movingTrains = new LinkedList<>();
		waitingTrains = new LinkedList<>();
	}

	public void markTracksDirty() {
		if (savedData != null)
			savedData.setDirty();
	}

	public void addTrain(Train train) {
		trains.put(train.id, train);
		movingTrains.add(train);
	}

	public void removeTrain(UUID id) {
		Train removed = trains.remove(id);
		if (removed == null)
			return;
		movingTrains.remove(removed);
		waitingTrains.remove(removed);
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
			graph.tickPoints(true);

		tickTrains(level);
		
		for (TrackGraph graph : trackNetworks.values())
			graph.tickPoints(false);

//		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_K))
//			trackNetworks.values()
//				.forEach(TrackGraph::debugViewReserved);
//		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_J) && AllKeys.altDown())
//			trackNetworks.values()
//				.forEach(TrackGraph::debugViewNodes);
	}

	private void tickTrains(Level level) {
		// keeping two lists ensures a tick order starting at longest waiting
		for (Train train : waitingTrains)
			train.earlyTick(level);
		for (Train train : movingTrains)
			train.earlyTick(level);
		for (Train train : waitingTrains)
			train.tick(level);
		for (Train train : movingTrains)
			train.tick(level);

		for (Iterator<Train> iterator = waitingTrains.iterator(); iterator.hasNext();) {
			Train train = iterator.next();
			if (train.navigation.waitingForSignal != null)
				continue;
			movingTrains.add(train);
			iterator.remove();
		}

		for (Iterator<Train> iterator = movingTrains.iterator(); iterator.hasNext();) {
			Train train = iterator.next();
			if (train.navigation.waitingForSignal == null)
				continue;
			waitingTrains.add(train);
			iterator.remove();
		}
	}

	public void clientTick() {
		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_H) && !AllKeys.altDown())
			trackNetworks.values()
				.forEach(TrackGraph::debugViewSignalData);
		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_J) && !AllKeys.altDown())
			trackNetworks.values()
				.forEach(TrackGraph::debugViewNodes);
	}

	public GlobalRailwayManager sided(LevelAccessor level) {
		if (level != null && !level.isClientSide())
			return this;
		MutableObject<GlobalRailwayManager> m = new MutableObject<>();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> clientManager(m));
		return m.getValue();
	}

	@OnlyIn(Dist.CLIENT)
	private void clientManager(MutableObject<GlobalRailwayManager> m) {
		m.setValue(CreateClient.RAILWAYS);
	}

}
