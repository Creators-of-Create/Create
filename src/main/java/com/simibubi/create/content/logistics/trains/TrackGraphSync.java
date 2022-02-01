package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class TrackGraphSync {

	//

	public void nodeAdded(TrackGraph graph, TrackNode node) {
		flushPacket(graph.id);
		currentPacket.addedNodes.put(node.getNetId(), Pair.of(node.getLocation(), node.getNormal()));
	}

	public void edgeAdded(TrackGraph graph, TrackNode node1, TrackNode node2, TrackEdge edge) {
		flushPacket(graph.id);
		currentPacket.addedEdges.add(Pair.of(Couple.create(node1.getNetId(), node2.getNetId()), edge));
	}

	public void nodeRemoved(TrackGraph graph, TrackNode node) {
		flushPacket(graph.id);
		if (currentPacket.addedNodes.remove(node.getNetId()) == null)
			currentPacket.removedNodes.add(node.getNetId());
	}

	public void graphSplit(TrackGraph graph, Set<TrackGraph> additional) {
		flushPacket(graph.id);
		additional.forEach(rg -> currentPacket.splitSubGraphs.put(rg.nodesById.keySet()
			.stream()
			.findFirst()
			.get(), rg.id));
	}

	public void graphRemoved(TrackGraph graph) {
		flushPacket(graph.id);
		currentPacket.delete = true;
	}

	public void finish() {
		flushPacket(null);
	}

	//

	private RailGraphSyncPacket currentPacket;

	public void sendFullGraphTo(TrackGraph graph, ServerPlayer player) {
		// TODO ensure packet size limit

		RailGraphSyncPacket packet = new RailGraphSyncPacket(graph.id);
		for (TrackNode node : graph.nodes.values()) {
			packet.addedNodes.put(node.getNetId(), Pair.of(node.getLocation(), node.getNormal()));
			if (!graph.connectionsByNode.containsKey(node))
				continue;
			graph.connectionsByNode.get(node)
				.forEach((node2, edge) -> packet.addedEdges
					.add(Pair.of(Couple.create(node.getNetId(), node2.getNetId()), edge)));
		}

		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	private void flushPacket(@Nullable UUID graphId) {
		if (currentPacket != null) {
			if (currentPacket.graphId.equals(graphId))
				return;
			AllPackets.channel.send(PacketDistributor.ALL.noArg(), currentPacket);
			currentPacket = null;
		}

		if (graphId != null)
			currentPacket = new RailGraphSyncPacket(graphId);
	}

	public static class RailGraphSyncPacket extends SimplePacketBase {

		UUID graphId;
		Map<Integer, Pair<TrackNodeLocation, Vec3>> addedNodes;
		List<Pair<Couple<Integer>, TrackEdge>> addedEdges;
		List<Integer> removedNodes;
		Map<Integer, UUID> splitSubGraphs;
		boolean delete;

		public RailGraphSyncPacket(UUID graphId) {
			this.graphId = graphId;
			addedNodes = new HashMap<>();
			addedEdges = new ArrayList<>();
			removedNodes = new ArrayList<>();
			splitSubGraphs = new HashMap<>();
			delete = false;
		}

		public RailGraphSyncPacket(FriendlyByteBuf buffer) {
			int size;

			graphId = buffer.readUUID();
			delete = buffer.readBoolean();

			if (delete)
				return;

			addedNodes = new HashMap<>();
			addedEdges = new ArrayList<>();
			removedNodes = new ArrayList<>();
			splitSubGraphs = new HashMap<>();

			size = buffer.readVarInt();
			for (int i = 0; i < size; i++)
				removedNodes.add(buffer.readVarInt());

			size = buffer.readVarInt();
			for (int i = 0; i < size; i++)
				addedNodes.put(buffer.readVarInt(),
					Pair.of(TrackNodeLocation.fromPackedPos(buffer.readBlockPos()), VecHelper.read(buffer)));

			size = buffer.readVarInt();
			for (int i = 0; i < size; i++)
				addedEdges.add(Pair.of(Couple.create(buffer::readVarInt), TrackEdge.read(buffer)));

			size = buffer.readVarInt();
			for (int i = 0; i < size; i++)
				splitSubGraphs.put(buffer.readVarInt(), buffer.readUUID());
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeUUID(graphId);
			buffer.writeBoolean(delete);
			if (delete)
				return;

			buffer.writeVarInt(removedNodes.size());
			removedNodes.forEach(buffer::writeVarInt);

			buffer.writeVarInt(addedNodes.size());
			addedNodes.forEach((node, loc) -> {
				buffer.writeVarInt(node);
				buffer.writeBlockPos(new BlockPos(loc.getFirst()));
				VecHelper.write(loc.getSecond(), buffer);
			});

			buffer.writeVarInt(addedEdges.size());
			addedEdges.forEach(pair -> {
				pair.getFirst()
					.forEach(buffer::writeVarInt);
				pair.getSecond()
					.write(buffer);
			});

			buffer.writeVarInt(splitSubGraphs.size());
			splitSubGraphs.forEach((node, uuid) -> {
				buffer.writeVarInt(node);
				buffer.writeUUID(uuid);
			});
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get()
				.enqueueWork(() -> {
					GlobalRailwayManager manager = CreateClient.RAILWAYS;
					TrackGraph railGraph = manager.getOrCreateGraph(graphId);

					if (delete) {
						manager.removeGraph(railGraph);
						return;
					}

					for (int nodeId : removedNodes) {
						TrackNode node = railGraph.getNode(nodeId);
						if (node != null)
							railGraph.removeNode(node.getLocation());
					}

					for (Entry<Integer, Pair<TrackNodeLocation, Vec3>> entry : addedNodes.entrySet())
						railGraph.createSpecificNode(entry.getValue()
							.getFirst(), entry.getKey(),
							entry.getValue()
								.getSecond());

					for (Pair<Couple<Integer>, TrackEdge> pair : addedEdges) {
						Couple<TrackNode> nodes = pair.getFirst()
							.map(railGraph::getNode);
						if (nodes.getFirst() != null && nodes.getSecond() != null)
							railGraph.putConnection(nodes.getFirst(), nodes.getSecond(), pair.getSecond());
					}

					if (!splitSubGraphs.isEmpty())
						railGraph.findDisconnectedGraphs(splitSubGraphs)
							.forEach(manager::putGraph);

				});
			context.get()
				.setPacketHandled(true);
		}

	}

}
