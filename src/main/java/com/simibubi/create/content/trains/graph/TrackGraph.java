package com.simibubi.create.content.trains.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class TrackGraph {

	public static final AtomicInteger graphNetIdGenerator = new AtomicInteger();
	public static final AtomicInteger nodeNetIdGenerator = new AtomicInteger();

	public UUID id;
	public Color color;

	Map<TrackNodeLocation, TrackNode> nodes;
	Map<Integer, TrackNode> nodesById;
	Map<TrackNode, Map<TrackNode, TrackEdge>> connectionsByNode;
	EdgePointStorage edgePoints;
	Map<ResourceKey<Level>, TrackGraphBounds> bounds;

	List<TrackEdge> deferredIntersectionUpdates;

	int netId;
	int checksum = 0;

	public TrackGraph() {
		this(UUID.randomUUID());
	}

	public TrackGraph(UUID graphID) {
		setId(graphID);
		nodes = new HashMap<>();
		nodesById = new HashMap<>();
		bounds = new HashMap<>();
		connectionsByNode = new IdentityHashMap<>();
		edgePoints = new EdgePointStorage();
		deferredIntersectionUpdates = new ArrayList<>();
		netId = nextGraphId();
	}

	//

	public <T extends TrackEdgePoint> void addPoint(EdgePointType<T> type, T point) {
		edgePoints.put(type, point);
		EdgePointManager.onEdgePointAdded(this, point, type);
		Create.RAILWAYS.sync.pointAdded(this, point);
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
		Create.RAILWAYS.sync.pointRemoved(this, removed);
		markDirty();
		return removed;
	}

	public void tickPoints(boolean preTrains) {
		edgePoints.tick(this, preTrains);
	}

	//

	public TrackGraphBounds getBounds(Level level) {
		return bounds.computeIfAbsent(level.dimension(), dim -> new TrackGraphBounds(this, dim));
	}

	public void invalidateBounds() {
		checksum = 0;
		bounds.clear();
	}

	//

	public Set<TrackNodeLocation> getNodes() {
		return nodes.keySet();
	}

	public TrackNode locateNode(Level level, Vec3 position) {
		return locateNode(new TrackNodeLocation(position).in(level));
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
		invalidateBounds();
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
		invalidateBounds();

		if (!connectionsByNode.containsKey(removed))
			return true;

		Map<TrackNode, TrackEdge> connections = connectionsByNode.remove(removed);
		for (Entry<TrackNode, TrackEdge> entry : connections.entrySet()) {
			TrackEdge trackEdge = entry.getValue();
			EdgeData edgeData = trackEdge.getEdgeData();
			for (TrackEdgePoint point : edgeData.getPoints()) {
				if (level != null)
					point.invalidate(level);
				edgePoints.remove(point.getType(), point.getId());
			}
			if (level != null) {
				TrackNode otherNode = entry.getKey();
				for (TrackEdgeIntersection intersection : edgeData.getIntersections()) {
					Couple<TrackNodeLocation> target = intersection.target;
					TrackGraph graph = Create.RAILWAYS.getGraph(level, target.getFirst());
					if (graph != null)
						graph.removeIntersection(intersection, removed, otherNode);
				}
			}
		}

		for (TrackNode railNode : connections.keySet())
			if (connectionsByNode.containsKey(railNode))
				connectionsByNode.get(railNode)
					.remove(removed);

		return true;
	}

	private void removeIntersection(TrackEdgeIntersection intersection, TrackNode targetNode1, TrackNode targetNode2) {
		TrackNode node1 = locateNode(intersection.target.getFirst());
		TrackNode node2 = locateNode(intersection.target.getSecond());
		if (node1 == null || node2 == null)
			return;

		Map<TrackNode, TrackEdge> from1 = getConnectionsFrom(node1);
		if (from1 != null) {
			TrackEdge edge = from1.get(node2);
			if (edge != null)
				edge.getEdgeData()
					.removeIntersection(this, intersection.id);
		}

		Map<TrackNode, TrackEdge> from2 = getConnectionsFrom(node2);
		if (from2 != null) {
			TrackEdge edge = from2.get(node1);
			if (edge != null)
				edge.getEdgeData()
					.removeIntersection(this, intersection.id);
		}
	}

	public static int nextNodeId() {
		return nodeNetIdGenerator.incrementAndGet();
	}

	public static int nextGraphId() {
		return graphNetIdGenerator.incrementAndGet();
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
			if (toOther.putConnection(n1, n2, edge)) {
				Create.RAILWAYS.sync.edgeAdded(toOther, n1, n2, edge);
				Create.RAILWAYS.sync.edgeDataChanged(toOther, n1, n2, edge);
			}
		}));

		edgePoints.transferAll(toOther, toOther.edgePoints);
		nodes.clear();
		connectionsByNode.clear();
		toOther.invalidateBounds();

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

	public Set<TrackGraph> findDisconnectedGraphs(@Nullable LevelAccessor level,
		@Nullable Map<Integer, Pair<Integer, UUID>> splitSubGraphs) {
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
					if (splitSubGraphs != null && splitSubGraphs.containsKey(currentNode.getNetId())) {
						Pair<Integer, UUID> ids = splitSubGraphs.get(currentNode.getNetId());
						target.setId(ids.getSecond());
						target.netId = ids.getFirst();
					}
					transfer(level, currentNode, target);
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

	public void setNetId(int id) {
		this.netId = id;
	}

	public int getChecksum() {
		if (checksum == 0)
			checksum = nodes.values()
				.stream()
				.collect(Collectors.summingInt(TrackNode::getNetId));
		return checksum;
	}

	public void transfer(LevelAccessor level, TrackNode node, TrackGraph target) {
		target.addNode(node);
		target.invalidateBounds();

		TrackNodeLocation nodeLoc = node.getLocation();
		Map<TrackNode, TrackEdge> connections = getConnectionsFrom(node);
		Map<UUID, Train> trains = Create.RAILWAYS.sided(level).trains;

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

		if (level != null)
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
		invalidateBounds();
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public Map<TrackNode, TrackEdge> getConnectionsFrom(TrackNode node) {
		if (node == null)
			return null;
		return connectionsByNode.getOrDefault(node, new HashMap<>());
	}

	public TrackEdge getConnection(Couple<TrackNode> nodes) {
		Map<TrackNode, TrackEdge> connectionsFrom = getConnectionsFrom(nodes.getFirst());
		if (connectionsFrom == null)
			return null;
		return connectionsFrom.get(nodes.getSecond());
	}

	public void connectNodes(LevelAccessor reader, DiscoveredLocation location, DiscoveredLocation location2,
		@Nullable BezierConnection turn) {
		TrackNode node1 = nodes.get(location);
		TrackNode node2 = nodes.get(location2);

		boolean bezier = turn != null;
		TrackMaterial material = bezier ? turn.getMaterial() : location2.materialA;
		TrackEdge edge = new TrackEdge(node1, node2, turn, material);
		TrackEdge edge2 = new TrackEdge(node2, node1, bezier ? turn.secondary() : null, material);

		for (TrackGraph graph : Create.RAILWAYS.trackNetworks.values()) {
			for (TrackNode otherNode1 : graph.nodes.values()) {
				Map<TrackNode, TrackEdge> connections = graph.connectionsByNode.get(otherNode1);
				if (connections == null)
					continue;
				for (Entry<TrackNode, TrackEdge> entry : connections.entrySet()) {
					TrackNode otherNode2 = entry.getKey();
					TrackEdge otherEdge = entry.getValue();

					if (graph == this)
						if (otherNode1 == node1 || otherNode2 == node1 || otherNode1 == node2 || otherNode2 == node2)
							continue;

					if (edge == otherEdge)
						continue;
					if (!bezier && !otherEdge.isTurn())
						continue;
					if (otherEdge.isTurn() && otherEdge.turn.isPrimary())
						continue;

					Collection<double[]> intersections =
						edge.getIntersection(node1, node2, otherEdge, otherNode1, otherNode2);

					UUID id = UUID.randomUUID();
					for (double[] intersection : intersections) {
						double s = intersection[0];
						double t = intersection[1];
						edge.edgeData.addIntersection(this, id, s, otherNode1, otherNode2, t);
						edge2.edgeData.addIntersection(this, id, edge.getLength() - s, otherNode1, otherNode2, t);
						otherEdge.edgeData.addIntersection(graph, id, t, node1, node2, s);
						TrackEdge otherEdge2 = graph.getConnection(Couple.create(otherNode2, otherNode1));
						if (otherEdge2 != null)
							otherEdge2.edgeData.addIntersection(graph, id, otherEdge.getLength() - t, node1, node2, s);
					}
				}
			}
		}

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

	public boolean putConnection(TrackNode node1, TrackNode node2, TrackEdge edge) {
		Map<TrackNode, TrackEdge> connections = connectionsByNode.computeIfAbsent(node1, n -> new IdentityHashMap<>());
		if (connections.containsKey(node2) && connections.get(node2)
			.getEdgeData()
			.hasPoints())
			return false;
		return connections.put(node2, edge) == null;
	}

	public float distanceToLocationSqr(Level level, Vec3 location) {
		float nearest = Float.MAX_VALUE;
		for (TrackNodeLocation tnl : nodes.keySet()) {
			if (!Objects.equals(tnl.dimension, level.dimension()))
				continue;
			nearest = Math.min(nearest, (float) tnl.getLocation()
				.distanceToSqr(location));
		}
		return nearest;
	}

	public void deferIntersectionUpdate(TrackEdge edge) {
		deferredIntersectionUpdates.add(edge);
	}

	public void resolveIntersectingEdgeGroups(Level level) {
		for (TrackEdge edge : deferredIntersectionUpdates) {
			if (!connectionsByNode.containsKey(edge.node1) || edge != connectionsByNode.get(edge.node1)
				.get(edge.node2))
				continue;
			EdgeData edgeData = edge.getEdgeData();
			for (TrackEdgeIntersection intersection : edgeData.getIntersections()) {
				UUID groupId = edgeData.getGroupAtPosition(this, intersection.location);
				Couple<TrackNodeLocation> target = intersection.target;
				TrackGraph graph = Create.RAILWAYS.getGraph(level, target.getFirst());
				if (graph == null)
					continue;

				TrackNode node1 = graph.locateNode(target.getFirst());
				TrackNode node2 = graph.locateNode(target.getSecond());
				Map<TrackNode, TrackEdge> connectionsFrom = graph.getConnectionsFrom(node1);
				if (connectionsFrom == null)
					continue;
				TrackEdge otherEdge = connectionsFrom.get(node2);
				if (otherEdge == null)
					continue;
				UUID otherGroupId = otherEdge.getEdgeData()
					.getGroupAtPosition(graph, intersection.targetLocation);

				SignalEdgeGroup group = Create.RAILWAYS.signalEdgeGroups.get(groupId);
				SignalEdgeGroup otherGroup = Create.RAILWAYS.signalEdgeGroups.get(otherGroupId);
				if (group == null || otherGroup == null || groupId == null || otherGroupId == null)
					continue;

				intersection.groupId = groupId;
				group.putIntersection(intersection.id, otherGroupId);
				otherGroup.putIntersection(intersection.id, groupId);
			}
		}
		deferredIntersectionUpdates.clear();
	}

	public void markDirty() {
		Create.RAILWAYS.markTracksDirty();
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("Id", id);
		tag.putInt("Color", color.getRGB());

		Map<TrackNode, Integer> indexTracker = new HashMap<>();
		ListTag nodesList = new ListTag();

		int i = 0;
		for (TrackNode railNode : nodes.values()) {
			indexTracker.put(railNode, i);
			CompoundTag nodeTag = new CompoundTag();
			nodeTag.put("Location", railNode.getLocation()
				.write(dimensions));
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
				connectionTag.put("EdgeData", edge.write(dimensions));
				connectionsList.add(connectionTag);
			});
			nodeTag.put("Connections", connectionsList);
		});

		tag.put("Nodes", nodesList);
		tag.put("Points", edgePoints.write(dimensions));
		return tag;
	}

	public static TrackGraph read(CompoundTag tag, DimensionPalette dimensions) {
		TrackGraph graph = new TrackGraph(tag.getUUID("Id"));
		graph.color = new Color(tag.getInt("Color"));
		graph.edgePoints.read(tag.getCompound("Points"), dimensions);

		Map<Integer, TrackNode> indexTracker = new HashMap<>();
		ListTag nodesList = tag.getList("Nodes", Tag.TAG_COMPOUND);

		int i = 0;
		for (Tag t : nodesList) {
			CompoundTag nodeTag = (CompoundTag) t;
			TrackNodeLocation location = TrackNodeLocation.read(nodeTag.getCompound("Location"), dimensions);
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
				TrackEdge edge = TrackEdge.read(node1, node2, c.getCompound("EdgeData"), graph, dimensions);
				graph.putConnection(node1, node2, edge);
			});
		}

		return graph;
	}

}
