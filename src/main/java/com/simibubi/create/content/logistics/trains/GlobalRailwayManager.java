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

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.entity.TrainPacket;
import com.simibubi.create.content.logistics.trains.management.display.GlobalTrainDisplayData;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroup;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
			ArrayList<SignalEdgeGroup> asList = new ArrayList<>(signalEdgeGroups.values());
			sync.sendEdgeGroups(asList.stream()
				.map(g -> g.id)
				.toList(),
				asList.stream()
					.map(g -> g.color)
					.toList(),
				serverPlayer);
			for (Train train : trains.values())
				AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> serverPlayer),
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
		GlobalTrainDisplayData.statusByDestination.clear();
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

	public TrackGraph getOrCreateGraph(UUID graphID, int netId) {
		return trackNetworks.computeIfAbsent(graphID, uid -> {
			TrackGraph trackGraph = new TrackGraph(graphID);
			trackGraph.netId = netId;
			return trackGraph;
		});
	}

	public void putGraphWithDefaultGroup(TrackGraph graph) {
		SignalEdgeGroup group = new SignalEdgeGroup(graph.id);
		signalEdgeGroups.put(graph.id, group.asFallback());
		sync.edgeGroupCreated(graph.id, group.color);
		putGraph(graph);
	}

	public void putGraph(TrackGraph graph) {
		trackNetworks.put(graph.id, graph);
		markTracksDirty();
	}

	public void removeGraphAndGroup(TrackGraph graph) {
		signalEdgeGroups.remove(graph.id);
		sync.edgeGroupRemoved(graph.id);
		removeGraph(graph);
	}

	public void removeGraph(TrackGraph graph) {
		trackNetworks.remove(graph.id);
		markTracksDirty();
	}

	public void updateSplitGraph(LevelAccessor level, TrackGraph graph) {
		Set<TrackGraph> disconnected = graph.findDisconnectedGraphs(level, null);
		disconnected.forEach(this::putGraphWithDefaultGroup);
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
		if (level.dimension() != Level.OVERWORLD)
			return;

		for (SignalEdgeGroup group : signalEdgeGroups.values()) {
			group.trains.clear();
			group.reserved = null;
		}

		for (TrackGraph graph : trackNetworks.values()) {
			graph.tickPoints(true);
			graph.resolveIntersectingEdgeGroups(level);
		}

		tickTrains(level);

		for (TrackGraph graph : trackNetworks.values())
			graph.tickPoints(false);

		GlobalTrainDisplayData.updateTick = level.getGameTime() % 100 == 0;
		if (GlobalTrainDisplayData.updateTick)
			GlobalTrainDisplayData.refresh();

//		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_H) && AllKeys.altDown())
//			for (TrackGraph trackGraph : trackNetworks.values())
//				TrackGraphVisualizer.debugViewSignalData(trackGraph);
//		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_J) && AllKeys.altDown())
//			for (TrackGraph trackGraph : trackNetworks.values())
//				TrackGraphVisualizer.debugViewNodes(trackGraph);
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

			if (train.invalid) {
				iterator.remove();
				trains.remove(train.id);
				AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new TrainPacket(train, false));
				continue;
			}

			if (train.navigation.waitingForSignal != null)
				continue;
			movingTrains.add(train);
			iterator.remove();
		}

		for (Iterator<Train> iterator = movingTrains.iterator(); iterator.hasNext();) {
			Train train = iterator.next();

			if (train.invalid) {
				iterator.remove();
				trains.remove(train.id);
				AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new TrainPacket(train, false));
				continue;
			}

			if (train.navigation.waitingForSignal == null)
				continue;
			waitingTrains.add(train);
			iterator.remove();
		}

	}

	public void tickSignalOverlay() {
		if (!isTrackGraphDebugActive())
			for (TrackGraph trackGraph : trackNetworks.values())
				TrackGraphVisualizer.visualiseSignalEdgeGroups(trackGraph);
	}

	public void clientTick() {
		if (isTrackGraphDebugActive())
			for (TrackGraph trackGraph : trackNetworks.values())
				TrackGraphVisualizer.debugViewGraph(trackGraph);
	}
	
	private static boolean isTrackGraphDebugActive() {
		return KineticDebugger.isF3DebugModeActive() && AllConfigs.client().showTrackGraphOnF3.get();
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
