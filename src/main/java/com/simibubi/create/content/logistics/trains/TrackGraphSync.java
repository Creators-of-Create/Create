package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.EdgeGroupColor;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroupPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.TrackEdgePoint;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class TrackGraphSync {

	List<TrackGraphPacket> queuedPackets = new ArrayList<>();
	int rollCallIn;

	public void serverTick() {
		flushGraphPacket();

		if (!queuedPackets.isEmpty()) {
			for (TrackGraphPacket packet : queuedPackets) {
				if (!packet.packetDeletesGraph && !Create.RAILWAYS.trackNetworks.containsKey(packet.graphId))
					continue;
				AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), packet);
				rollCallIn = 3;
			}

			queuedPackets.clear();
		}

		if (rollCallIn <= 0)
			return;
		rollCallIn--;
		if (rollCallIn > 0)
			return;

		sendRollCall();
	}

	//

	public void nodeAdded(TrackGraph graph, TrackNode node) {
		flushGraphPacket(graph);
		currentGraphSyncPacket.addedNodes.put(node.getNetId(), Pair.of(node.getLocation(), node.getNormal()));
		currentPayload++;
	}

	public void edgeAdded(TrackGraph graph, TrackNode node1, TrackNode node2, TrackEdge edge) {
		flushGraphPacket(graph);
		currentGraphSyncPacket.addedEdges
			.add(Pair.of(Couple.create(node1.getNetId(), node2.getNetId()), edge.getTurn()));
		currentPayload++;
	}

	public void pointAdded(TrackGraph graph, TrackEdgePoint point) {
		flushGraphPacket(graph);
		currentGraphSyncPacket.addedEdgePoints.add(point);
		currentPayload++;
	}

	public void pointRemoved(TrackGraph graph, TrackEdgePoint point) {
		flushGraphPacket(graph);
		currentGraphSyncPacket.removedEdgePoints.add(point.getId());
		currentPayload++;
	}

	public void nodeRemoved(TrackGraph graph, TrackNode node) {
		flushGraphPacket(graph);
		int nodeId = node.getNetId();
		if (currentGraphSyncPacket.addedNodes.remove(nodeId) == null)
			currentGraphSyncPacket.removedNodes.add(nodeId);
		currentGraphSyncPacket.addedEdges.removeIf(pair -> {
			Couple<Integer> ids = pair.getFirst();
			return ids.getFirst()
				.intValue() == nodeId
				|| ids.getSecond()
					.intValue() == nodeId;
		});
	}

	public void graphSplit(TrackGraph graph, Set<TrackGraph> additional) {
		flushGraphPacket(graph);
		additional.forEach(rg -> currentGraphSyncPacket.splitSubGraphs.put(rg.nodesById.keySet()
			.stream()
			.findFirst()
			.get(), Pair.of(rg.netId, rg.id)));
	}

	public void graphRemoved(TrackGraph graph) {
		flushGraphPacket(graph);
		currentGraphSyncPacket.packetDeletesGraph = true;
	}

	//

	public void sendEdgeGroups(List<UUID> ids, List<EdgeGroupColor> colors, ServerPlayer player) {
		AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player),
			new SignalEdgeGroupPacket(ids, colors, true));
	}

	public void edgeGroupCreated(UUID id, EdgeGroupColor color) {
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new SignalEdgeGroupPacket(id, color));
	}

	public void edgeGroupRemoved(UUID id) {
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(),
			new SignalEdgeGroupPacket(ImmutableList.of(id), Collections.emptyList(), false));
	}

	//

	public void edgeDataChanged(TrackGraph graph, TrackNode node1, TrackNode node2, TrackEdge edge) {
		flushGraphPacket(graph);
		currentGraphSyncPacket.syncEdgeData(node1, node2, edge);
		currentPayload++;
	}

	public void edgeDataChanged(TrackGraph graph, TrackNode node1, TrackNode node2, TrackEdge edge, TrackEdge edge2) {
		flushGraphPacket(graph);
		currentGraphSyncPacket.syncEdgeData(node1, node2, edge);
		currentGraphSyncPacket.syncEdgeData(node2, node1, edge2);
		currentPayload++;
	}

	public void sendFullGraphTo(TrackGraph graph, ServerPlayer player) {
		TrackGraphSyncPacket packet = new TrackGraphSyncPacket(graph.id, graph.netId);
		packet.fullWipe = true;
		int sent = 0;

		for (TrackNode node : graph.nodes.values()) {
			TrackGraphSyncPacket currentPacket = packet;
			currentPacket.addedNodes.put(node.getNetId(), Pair.of(node.getLocation(), node.getNormal()));
			if (sent++ < 1000)
				continue;

			sent = 0;
			packet = flushAndCreateNew(graph, player, packet);
		}

		for (TrackNode node : graph.nodes.values()) {
			TrackGraphSyncPacket currentPacket = packet;
			if (!graph.connectionsByNode.containsKey(node))
				continue;
			graph.connectionsByNode.get(node)
				.forEach((node2, edge) -> {
					Couple<Integer> key = Couple.create(node.getNetId(), node2.getNetId());
					currentPacket.addedEdges.add(Pair.of(key, edge.getTurn()));
					currentPacket.syncEdgeData(node, node2, edge);
				});

			if (sent++ < 1000)
				continue;

			sent = 0;
			packet = flushAndCreateNew(graph, player, packet);
		}

		for (EdgePointType<?> type : EdgePointType.TYPES.values()) {
			for (TrackEdgePoint point : graph.getPoints(type)) {
				packet.addedEdgePoints.add(point);

				if (sent++ < 1000)
					continue;

				sent = 0;
				packet = flushAndCreateNew(graph, player, packet);
			}
		}

		if (sent > 0)
			flushAndCreateNew(graph, player, packet);
	}

	private void sendRollCall() {
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new TrackGraphRollCallPacket());
	}

	private TrackGraphSyncPacket flushAndCreateNew(TrackGraph graph, ServerPlayer player, TrackGraphSyncPacket packet) {
		AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), packet);
		packet = new TrackGraphSyncPacket(graph.id, graph.netId);
		return packet;
	}

	//

	private TrackGraphSyncPacket currentGraphSyncPacket;
	private int currentPayload;

	private void flushGraphPacket() {
		flushGraphPacket(null, 0);
	}

	private void flushGraphPacket(TrackGraph graph) {
		flushGraphPacket(graph.id, graph.netId);
	}

	private void flushGraphPacket(@Nullable UUID graphId, int netId) {
		if (currentGraphSyncPacket != null) {
			if (currentGraphSyncPacket.graphId.equals(graphId) && currentPayload < 1000)
				return;
			queuedPackets.add(currentGraphSyncPacket);
			currentGraphSyncPacket = null;
			currentPayload = 0;
		}

		if (graphId != null) {
			currentGraphSyncPacket = new TrackGraphSyncPacket(graphId, netId);
			currentPayload = 0;
		}
	}

}
