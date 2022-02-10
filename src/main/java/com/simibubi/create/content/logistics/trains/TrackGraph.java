package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class TrackGraph {

	public static final AtomicInteger netIdGenerator = new AtomicInteger();

	public UUID id;
	public Color color;

	Map<TrackNodeLocation, TrackNode> nodes;
	Map<Integer, TrackNode> nodesById;
	Map<TrackNode, Map<TrackNode, TrackEdge>> connectionsByNode;

	Map<UUID, GlobalStation> stations;

	public TrackGraph() {
		this(UUID.randomUUID());
	}

	public TrackGraph(UUID graphID) {
		id = graphID;
		nodes = new HashMap<>();
		nodesById = new HashMap<>();
		connectionsByNode = new IdentityHashMap<>();
		color = Color.rainbowColor(new Random(graphID.getLeastSignificantBits()).nextInt());
		stations = new HashMap<>();
	}

	//

	@Nullable
	public GlobalStation getStation(UUID id) {
		return stations.get(id);
	}

	public Collection<GlobalStation> getStations() {
		return stations.values();
	}

	public void removeStation(UUID id) {
		stations.remove(id);
		markDirty();
	}

	public void addStation(GlobalStation station) {
		stations.put(station.id, station);
		markDirty();
	}

	//

	public Set<TrackNodeLocation> getNodes() {
		return nodes.keySet();
	}

	public TrackNode locateNode(Vec3 position) {
		return locateNode(new TrackNodeLocation(position));
	}

	public TrackNode locateNode(TrackNodeLocation position) {
		return nodes.get(position);
	}

	public TrackNode getNode(int netId) {
		return nodesById.get(netId);
	}

	public boolean createNode(DiscoveredLocation location) {
		if (!createSpecificNode(location, nextNodeId(), location.normal))
			return false;
		Create.RAILWAYS.sync.nodeAdded(this, nodes.get(location));
		markDirty();
		return true;
	}

	public boolean createSpecificNode(TrackNodeLocation location, int netId, Vec3 normal) {
		return addNode(new TrackNode(location, netId, normal));
	}

	public boolean addNode(TrackNode node) {
		if (nodes.putIfAbsent(node.getLocation(), node) != null)
			return false;
		nodesById.put(node.getNetId(), node);
		return true;
	}

	public boolean removeNode(@Nullable LevelAccessor level, TrackNodeLocation location) {
		TrackNode removed = nodes.remove(location);
		if (removed == null)
			return false;

		if (level != null) {
			for (Iterator<UUID> iterator = stations.keySet()
				.iterator(); iterator.hasNext();) {
				UUID uuid = iterator.next();
				GlobalStation globalStation = stations.get(uuid);
				Couple<TrackNodeLocation> loc = globalStation.edgeLocation;
				if (loc.getFirst()
					.equals(location)
					|| loc.getSecond()
						.equals(location)) {
					globalStation.migrate(level);
					iterator.remove();
				}
			}
		}

		Map<UUID, Train> trains = Create.RAILWAYS.trains;
		for (Iterator<UUID> iterator = trains.keySet()
			.iterator(); iterator.hasNext();) {
			UUID uuid = iterator.next();
			Train train = trains.get(uuid);
			if (train.graph != this)
				continue;
			if (train.isTravellingOn(removed))
				train.detachFromTracks();
		}

		nodesById.remove(removed.netId);
		if (!connectionsByNode.containsKey(removed))
			return true;

		Map<TrackNode, TrackEdge> connections = connectionsByNode.remove(removed);
		for (TrackNode railNode : connections.keySet())
			if (connectionsByNode.containsKey(railNode))
				connectionsByNode.get(railNode)
					.remove(removed);
		return true;
	}

	public static int nextNodeId() {
		return netIdGenerator.incrementAndGet();
	}

	public void transferAll(TrackGraph toOther) {
		toOther.nodes.putAll(nodes);
		toOther.nodesById.putAll(nodesById);
		toOther.connectionsByNode.putAll(connectionsByNode);
		for (GlobalStation globalStation : stations.values())
			toOther.addStation(globalStation);

		nodesById.forEach((id, node) -> Create.RAILWAYS.sync.nodeAdded(toOther, node));
		connectionsByNode.forEach(
			(node1, map) -> map.forEach((node2, edge) -> Create.RAILWAYS.sync.edgeAdded(toOther, node1, node2, edge)));
		markDirty();

		stations.clear();
		nodes.clear();
		nodesById.clear();
		connectionsByNode.clear();

		Map<UUID, Train> trains = Create.RAILWAYS.trains;
		for (Iterator<UUID> iterator = trains.keySet()
			.iterator(); iterator.hasNext();) {
			UUID uuid = iterator.next();
			Train train = trains.get(uuid);
			if (train.graph != this)
				continue;
			train.graph = toOther;
		}
	}

	public Set<TrackGraph> findDisconnectedGraphs(@Nullable Map<Integer, UUID> preAssignedIds) {
		Set<TrackGraph> dicovered = new HashSet<>();
		Set<TrackNodeLocation> vertices = new HashSet<>(nodes.keySet());
		List<TrackNodeLocation> frontier = new ArrayList<>();
		TrackGraph target = null;

		while (!vertices.isEmpty()) {
			if (target != null)
				dicovered.add(target);

			TrackNodeLocation start = vertices.stream()
				.findFirst()
				.get();
			frontier.add(start);
			vertices.remove(start);

			while (!frontier.isEmpty()) {
				TrackNodeLocation current = frontier.remove(0);
				TrackNode currentNode = locateNode(current);

				Map<TrackNode, TrackEdge> connections = getConnectionsFrom(currentNode);
				for (TrackNode connected : connections.keySet())
					if (vertices.remove(connected.getLocation()))
						frontier.add(connected.getLocation());

				if (target != null) {
					transfer(currentNode, target);
					if (preAssignedIds != null && preAssignedIds.containsKey(currentNode.getNetId()))
						target.id = preAssignedIds.get(currentNode.getNetId());
				}
			}

			frontier.clear();
			target = new TrackGraph();
		}

		return dicovered;
	}

	public void transfer(TrackNode node, TrackGraph target) {
		target.addNode(node);
		TrackNodeLocation location1 = node.getLocation();

		Map<TrackNode, TrackEdge> connections = getConnectionsFrom(node);
		if (!connections.isEmpty()) {
			target.connectionsByNode.put(node, connections);
			for (TrackNode entry : connections.keySet()) {
				for (Iterator<UUID> iterator = stations.keySet()
					.iterator(); iterator.hasNext();) {
					UUID uuid = iterator.next();
					GlobalStation globalStation = stations.get(uuid);
					Couple<TrackNodeLocation> loc = globalStation.edgeLocation;
					if (loc.getFirst()
						.equals(location1)
						&& loc.getSecond()
							.equals(entry.getLocation())) {
						target.addStation(globalStation);
						iterator.remove();
					}
				}
			}
		}

		Map<UUID, Train> trains = Create.RAILWAYS.trains;
		for (Iterator<UUID> iterator = trains.keySet()
			.iterator(); iterator.hasNext();) {
			UUID uuid = iterator.next();
			Train train = trains.get(uuid);
			if (train.graph != this)
				continue;
			if (!train.isTravellingOn(node))
				continue;
			train.graph = target;
		}

		nodes.remove(location1);
		nodesById.remove(node.getNetId());
		connectionsByNode.remove(node);
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public Map<TrackNode, TrackEdge> getConnectionsFrom(TrackNode node) {
		return connectionsByNode.getOrDefault(node, new HashMap<>());
	}

	public void connectNodes(TrackNodeLocation location, TrackNodeLocation location2, TrackEdge edge) {
		TrackNode node1 = nodes.get(location);
		TrackNode node2 = nodes.get(location2);
		TrackEdge edge2 = new TrackEdge(edge.turn != null ? edge.turn.secondary() : null);

		putConnection(node1, node2, edge);
		putConnection(node2, node1, edge2);

		Create.RAILWAYS.sync.edgeAdded(this, node1, node2, edge);
		Create.RAILWAYS.sync.edgeAdded(this, node2, node1, edge2);
		markDirty();
	}

	public void disconnectNodes(TrackNode node1, TrackNode node2) {
		Map<TrackNode, TrackEdge> map1 = connectionsByNode.get(node1);
		Map<TrackNode, TrackEdge> map2 = connectionsByNode.get(node2);
		if (map1 != null)
			map1.remove(node2);
		if (map2 != null)
			map2.remove(node1);
	}

	public void putConnection(TrackNode node1, TrackNode node2, TrackEdge edge) {
		connectionsByNode.computeIfAbsent(node1, n -> new IdentityHashMap<>())
			.put(node2, edge);
	}

	public void markDirty() {
		Create.RAILWAYS.markTracksDirty();
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("Id", id);
		tag.putInt("Color", color.getRGB());

		Map<TrackNode, Integer> indexTracker = new HashMap<>();
		ListTag nodesList = new ListTag();

		int i = 0;
		for (TrackNode railNode : nodes.values()) {
			indexTracker.put(railNode, i);
			CompoundTag nodeTag = new CompoundTag();
			nodeTag.put("Location", NbtUtils.writeBlockPos(new BlockPos(railNode.getLocation())));
			nodeTag.put("Normal", VecHelper.writeNBT(railNode.getNormal()));
			nodesList.add(nodeTag);
			i++;
		}

		connectionsByNode.forEach((node1, map) -> {
			Integer index1 = indexTracker.get(node1);
			if (index1 == null)
				return;
			CompoundTag nodeTag = (CompoundTag) nodesList.get(index1);
			ListTag connectionsList = new ListTag();
			map.forEach((node2, edge) -> {
				CompoundTag connectionTag = new CompoundTag();
				Integer index2 = indexTracker.get(node2);
				if (index2 == null)
					return;
				connectionTag.putInt("To", index2);
				connectionTag.put("EdgeData", edge.write());
				connectionsList.add(connectionTag);
			});
			nodeTag.put("Connections", connectionsList);
		});

		tag.put("Nodes", nodesList);
		tag.put("Stations", NBTHelper.writeCompoundList(stations.values(), GlobalStation::write));
		return tag;
	}

	public static TrackGraph read(CompoundTag tag) {
		TrackGraph graph = new TrackGraph(tag.getUUID("Id"));
		graph.color = new Color(tag.getInt("Color"));

		Map<Integer, TrackNode> indexTracker = new HashMap<>();
		ListTag nodesList = tag.getList("Nodes", Tag.TAG_COMPOUND);

		int i = 0;
		for (Tag t : nodesList) {
			CompoundTag nodeTag = (CompoundTag) t;
			TrackNodeLocation location =
				TrackNodeLocation.fromPackedPos(NbtUtils.readBlockPos(nodeTag.getCompound("Location")));
			Vec3 normal = VecHelper.readNBT(nodeTag.getList("Normal", Tag.TAG_DOUBLE));
			graph.createSpecificNode(location, nextNodeId(), normal);
			indexTracker.put(i, graph.locateNode(location));
			i++;
		}

		i = 0;
		for (Tag t : nodesList) {
			CompoundTag nodeTag = (CompoundTag) t;
			TrackNode node1 = indexTracker.get(i);
			i++;

			if (!nodeTag.contains("Connections"))
				continue;
			NBTHelper.iterateCompoundList(nodeTag.getList("Connections", Tag.TAG_COMPOUND), c -> {
				TrackNode node2 = indexTracker.get(c.getInt("To"));
				TrackEdge edge = TrackEdge.read(c.getCompound("EdgeData"));
				graph.putConnection(node1, node2, edge);
			});
		}

		NBTHelper.readCompoundList(tag.getList("Stations", Tag.TAG_COMPOUND), GlobalStation::new)
			.forEach(s -> graph.stations.put(s.id, s));
		return graph;
	}

	public void debugViewNodes() {
		Entity cameraEntity = Minecraft.getInstance().cameraEntity;
		if (cameraEntity == null)
			return;
		Vec3 camera = cameraEntity.getEyePosition();
		for (Entry<TrackNodeLocation, TrackNode> nodeEntry : nodes.entrySet()) {
			TrackNodeLocation nodeLocation = nodeEntry.getKey();
			TrackNode node = nodeEntry.getValue();
			if (nodeLocation == null)
				continue;

			Vec3 location = nodeLocation.getLocation();
			if (location.distanceTo(camera) > 50)
				continue;

			Vec3 yOffset = new Vec3(0, 3 / 16f, 0);
			Vec3 v1 = location.add(yOffset);
			Vec3 v2 = v1.add(node.normal.scale(0.125f));
			CreateClient.OUTLINER.showLine(Integer.valueOf(node.netId), v1, v2)
				.colored(Color.mixColors(Color.WHITE, color, 1))
				.lineWidth(1 / 4f);

			Map<TrackNode, TrackEdge> map = connectionsByNode.get(node);
			if (map == null)
				continue;

			int hashCode = node.hashCode();
			for (Entry<TrackNode, TrackEdge> entry : map.entrySet()) {
				TrackNode other = entry.getKey();

				if (other.hashCode() > hashCode)
					continue;

				TrackEdge edge = entry.getValue();
				if (!edge.isTurn()) {
					CreateClient.OUTLINER.showLine(edge, edge.getPosition(node, other, 0)
						.add(yOffset),
						edge.getPosition(node, other, 1)
							.add(yOffset))
						.colored(color)
						.lineWidth(1 / 16f);
					continue;
				}

				Vec3 previous = null;
				BezierConnection turn = edge.getTurn();
				for (int i = 0; i <= turn.getSegmentCount(); i++) {
					Vec3 current = edge.getPosition(node, other, i * 1f / turn.getSegmentCount());
					if (previous != null)
						CreateClient.OUTLINER.showLine(previous, previous.add(yOffset), current.add(yOffset))
							.colored(color)
							.lineWidth(1 / 16f);
					previous = current;
				}
			}
		}
	}

}
