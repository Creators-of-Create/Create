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

import org.lwjgl.glfw.GLFW;

import com.simibubi.create.AllKeys;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointManager;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointStorage;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.signal.EdgeData;
import com.simibubi.create.content.logistics.trains.management.signal.SignalBoundary;
import com.simibubi.create.content.logistics.trains.management.signal.SignalEdgeGroup;
import com.simibubi.create.content.logistics.trains.management.signal.TrackEdgePoint;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;
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
	EdgePointStorage edgePoints;

	public TrackGraph() {
		this(UUID.randomUUID());
	}

	public TrackGraph(UUID graphID) {
		setId(graphID);
		nodes = new HashMap<>();
		nodesById = new HashMap<>();
		connectionsByNode = new IdentityHashMap<>();
		edgePoints = new EdgePointStorage();
	}

	//

	public <T extends TrackEdgePoint> void addPoint(EdgePointType<T> type, T point) {
		edgePoints.put(type, point);
		EdgePointManager.onEdgePointAdded(this, point, type);
		markDirty();
	}

	public <T extends TrackEdgePoint> T getPoint(EdgePointType<T> type, UUID id) {
		return edgePoints.get(type, id);
	}

	public <T extends TrackEdgePoint> Collection<T> getPoints(EdgePointType<T> type) {
		return edgePoints.values(type);
	}

	public <T extends TrackEdgePoint> T removePoint(EdgePointType<T> type, UUID id) {
		T removed = edgePoints.remove(type, id);
		if (removed == null)
			return null;
		EdgePointManager.onEdgePointRemoved(this, removed, type);
		markDirty();
		return removed;
	}

	public void tickPoints() {
		edgePoints.tick(this);
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

	public boolean createNodeIfAbsent(DiscoveredLocation location) {
		if (!addNodeIfAbsent(new TrackNode(location, nextNodeId(), location.normal)))
			return false;
		TrackNode newNode = nodes.get(location);
		Create.RAILWAYS.sync.nodeAdded(this, newNode);
		markDirty();
		return true;
	}

	public void loadNode(TrackNodeLocation location, int netId, Vec3 normal) {
		addNode(new TrackNode(location, netId, normal));
	}

	public void addNode(TrackNode node) {
		TrackNodeLocation location = node.getLocation();
		if (nodes.containsKey(location))
			removeNode(null, location);
		nodes.put(location, node);
		nodesById.put(node.getNetId(), node);
	}

	public boolean addNodeIfAbsent(TrackNode node) {
		if (nodes.putIfAbsent(node.getLocation(), node) != null)
			return false;
		nodesById.put(node.getNetId(), node);
		return true;
	}

	public boolean removeNode(@Nullable LevelAccessor level, TrackNodeLocation location) {
		TrackNode removed = nodes.remove(location);
		if (removed == null)
			return false;

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
		for (TrackEdge trackEdge : connections.values())
			for (TrackEdgePoint point : trackEdge.getEdgeData()
				.getPoints()) {
				if (level != null)
					point.invalidate(level);
				edgePoints.remove(point.getType(), point.getId());
			}

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
		nodes.forEach((loc, node) -> {
			if (toOther.addNodeIfAbsent(node))
				Create.RAILWAYS.sync.nodeAdded(toOther, node);
		});

		connectionsByNode.forEach((node1, map) -> map.forEach((node2, edge) -> {
			TrackNode n1 = toOther.locateNode(node1.location);
			TrackNode n2 = toOther.locateNode(node2.location);
			if (n1 == null || n2 == null)
				return;
			toOther.putConnection(n1, n2, edge);
			Create.RAILWAYS.sync.edgeAdded(toOther, n1, n2, edge);
		}));

		edgePoints.transferAll(toOther.edgePoints);

		nodes.clear();
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
						target.setId(preAssignedIds.get(currentNode.getNetId()));
				}
			}

			frontier.clear();
			target = new TrackGraph();
		}

		return dicovered;
	}

	public void setId(UUID id) {
		this.id = id;
		color = Color.rainbowColor(new Random(id.getLeastSignificantBits()).nextInt());
	}

	public void transfer(TrackNode node, TrackGraph target) {
		target.addNode(node);

		TrackNodeLocation nodeLoc = node.getLocation();
		Map<TrackNode, TrackEdge> connections = getConnectionsFrom(node);
		Map<UUID, Train> trains = Create.RAILWAYS.trains;

		if (!connections.isEmpty()) {
			target.connectionsByNode.put(node, connections);
			for (TrackEdge entry : connections.values()) {
				EdgeData edgeData = entry.getEdgeData();
				for (TrackEdgePoint trackEdgePoint : edgeData.getPoints()) {
					target.edgePoints.put(trackEdgePoint.getType(), trackEdgePoint);
					edgePoints.remove(trackEdgePoint.getType(), trackEdgePoint.getId());
				}
			}
		}

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

		nodes.remove(nodeLoc);
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
		tag.put("Points", edgePoints.write());
		return tag;
	}

	public static TrackGraph read(CompoundTag tag) {
		TrackGraph graph = new TrackGraph(tag.getUUID("Id"));
		graph.color = new Color(tag.getInt("Color"));
		graph.edgePoints.read(tag.getCompound("Points"));

		Map<Integer, TrackNode> indexTracker = new HashMap<>();
		ListTag nodesList = tag.getList("Nodes", Tag.TAG_COMPOUND);

		int i = 0;
		for (Tag t : nodesList) {
			CompoundTag nodeTag = (CompoundTag) t;
			TrackNodeLocation location =
				TrackNodeLocation.fromPackedPos(NbtUtils.readBlockPos(nodeTag.getCompound("Location")));
			Vec3 normal = VecHelper.readNBT(nodeTag.getList("Normal", Tag.TAG_DOUBLE));
			graph.loadNode(location, nextNodeId(), normal);
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
				TrackEdge edge = TrackEdge.read(c.getCompound("EdgeData"), graph);
				graph.putConnection(node1, node2, edge);
			});
		}

		return graph;
	}

	public void debugViewSignalData() {
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

			Map<TrackNode, TrackEdge> map = connectionsByNode.get(node);
			if (map == null)
				continue;

			int hashCode = node.hashCode();
			for (Entry<TrackNode, TrackEdge> entry : map.entrySet()) {
				TrackNode other = entry.getKey();

				if (other.hashCode() > hashCode && !AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
					continue;
				Vec3 yOffset = new Vec3(0, (other.hashCode() > hashCode ? 6 : 4) / 16f, 0);

				TrackEdge edge = entry.getValue();
				EdgeData signalData = edge.getEdgeData();
				UUID singleGroup = signalData.singleSignalGroup;
				SignalEdgeGroup signalEdgeGroup =
					singleGroup == null ? null : Create.RAILWAYS.signalEdgeGroups.get(singleGroup);

				if (!edge.isTurn()) {
					Vec3 p1 = edge.getPosition(node, other, 0);
					Vec3 p2 = edge.getPosition(node, other, 1);

					if (signalData.hasPoints()) {
						double prev = 0;
						double length = edge.getLength(node, other);
						SignalBoundary prevBoundary = null;
						SignalEdgeGroup group = null;

						for (TrackEdgePoint trackEdgePoint : signalData.getPoints()) {
							if (trackEdgePoint instanceof GlobalStation) {
								Vec3 v1 = edge
									.getPosition(node, other,
										(trackEdgePoint.getLocationOn(node, other, edge) / length))
									.add(yOffset);
								Vec3 v2 = v1.add(node.normal.scale(3 / 16f));
								CreateClient.OUTLINER.showLine(trackEdgePoint.id, v1, v2)
									.colored(Color.mixColors(Color.WHITE, color, 1))
									.lineWidth(1 / 8f);
								continue;
							}
							if (!(trackEdgePoint instanceof SignalBoundary boundary))
								continue;

							prevBoundary = boundary;
							group = Create.RAILWAYS.signalEdgeGroups.get(boundary.getGroup(node));

							if (group != null)
								CreateClient.OUTLINER
									.showLine(Pair.of(boundary, edge),
										edge.getPosition(node, other, prev + (prev == 0 ? 0 : 1 / 16f / length))
											.add(yOffset),
										edge.getPosition(node, other,
											(prev = boundary.getLocationOn(node, other, edge) / length)
												- 1 / 16f / length)
											.add(yOffset))
									.colored(group.color.getRGB())
									.lineWidth(1 / 16f);

						}

						if (prevBoundary != null) {
							group = Create.RAILWAYS.signalEdgeGroups.get(prevBoundary.getGroup(other));
							if (group != null)
								CreateClient.OUTLINER
									.showLine(edge, edge.getPosition(node, other, prev + 1 / 16f / length)
										.add(yOffset), p2.add(yOffset))
									.colored(group.color.getRGB())
									.lineWidth(1 / 16f);
							continue;
						}
					}

					if (signalEdgeGroup == null)
						continue;
					CreateClient.OUTLINER.showLine(edge, p1.add(yOffset), p2.add(yOffset))
						.colored(signalEdgeGroup.color.getRGB())
						.lineWidth(1 / 16f);
					continue;
				}

				if (signalEdgeGroup == null)
					continue;

				Vec3 previous = null;
				BezierConnection turn = edge.getTurn();
				for (int i = 0; i <= turn.getSegmentCount(); i++) {
					Vec3 current = edge.getPosition(node, other, i * 1f / turn.getSegmentCount());
					if (previous != null)
						CreateClient.OUTLINER
							.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset))
							.colored(signalEdgeGroup.color.getRGB())
							.lineWidth(1 / 16f);
					previous = current;
				}
			}
		}
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
			Vec3 v2 = v1.add(node.normal.scale(3 / 16f));
			CreateClient.OUTLINER.showLine(Integer.valueOf(node.netId), v1, v2)
				.colored(Color.mixColors(Color.WHITE, color, 1))
				.lineWidth(1 / 8f);

			Map<TrackNode, TrackEdge> map = connectionsByNode.get(node);
			if (map == null)
				continue;

			int hashCode = node.hashCode();
			for (Entry<TrackNode, TrackEdge> entry : map.entrySet()) {
				TrackNode other = entry.getKey();

				if (other.hashCode() > hashCode && !AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
					continue;
				yOffset = new Vec3(0, (other.hashCode() > hashCode ? 6 : 4) / 16f, 0);

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
						CreateClient.OUTLINER
							.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset))
							.colored(color)
							.lineWidth(1 / 16f);
					previous = current;
				}
			}
		}
	}

}
