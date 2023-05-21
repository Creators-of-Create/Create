package com.simibubi.create.content.trains.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.edgePoint.EdgeData;
import com.simibubi.create.content.trains.edgePoint.EdgePointType;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackGraphSync;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

public class SignalPropagator {

	public static void onSignalRemoved(TrackGraph graph, SignalBoundary signal) {
		signal.sidesToUpdate.map($ -> false);
		for (boolean front : Iterate.trueAndFalse) {
			if (signal.sidesToUpdate.get(front))
				continue;
			UUID id = signal.groups.get(front);
			if (Create.RAILWAYS.signalEdgeGroups.remove(id) != null)
				Create.RAILWAYS.sync.edgeGroupRemoved(id);

			walkSignals(graph, signal, front, pair -> {
				TrackNode node1 = pair.getFirst();
				SignalBoundary boundary = pair.getSecond();
				boundary.queueUpdate(node1);
				return false;

			}, signalData -> {
				if (!signalData.hasSignalBoundaries()) {
					signalData.setSingleSignalGroup(graph, EdgeData.passiveGroup);
					return true;
				}
				return false;

			}, false);
		}
	}

	public static void notifySignalsOfNewNode(TrackGraph graph, TrackNode node) {
		List<Couple<TrackNode>> frontier = new ArrayList<>();
		frontier.add(Couple.create(node, null));
		walkSignals(graph, frontier, pair -> {
			TrackNode node1 = pair.getFirst();
			SignalBoundary boundary = pair.getSecond();
			boundary.queueUpdate(node1);
			return false;

		}, signalData -> {
			if (!signalData.hasSignalBoundaries()) {
				signalData.setSingleSignalGroup(graph, EdgeData.passiveGroup);
				return true;
			}
			return false;

		}, false);
	}

	public static void propagateSignalGroup(TrackGraph graph, SignalBoundary signal, boolean front) {
		Map<UUID, SignalEdgeGroup> globalGroups = Create.RAILWAYS.signalEdgeGroups;
		TrackGraphSync sync = Create.RAILWAYS.sync;

		SignalEdgeGroup group = new SignalEdgeGroup(UUID.randomUUID());
		UUID groupId = group.id;
		globalGroups.put(groupId, group);
		signal.setGroup(front, groupId);
		sync.pointAdded(graph, signal);

		walkSignals(graph, signal, front, pair -> {
			TrackNode node1 = pair.getFirst();
			SignalBoundary boundary = pair.getSecond();
			UUID currentGroup = boundary.getGroup(node1);
			if (currentGroup != null)
				if (globalGroups.remove(currentGroup) != null)
					sync.edgeGroupRemoved(currentGroup);
			boundary.setGroupAndUpdate(node1, groupId);
			sync.pointAdded(graph, boundary);
			return true;

		}, signalData -> {
			UUID singleSignalGroup = signalData.getSingleSignalGroup();
			if (singleSignalGroup != null)
				if (globalGroups.remove(singleSignalGroup) != null)
					sync.edgeGroupRemoved(singleSignalGroup);
			signalData.setSingleSignalGroup(graph, groupId);
			return true;

		}, false);

		group.resolveColor();
		sync.edgeGroupCreated(groupId, group.color);
	}

	public static Map<UUID, Boolean> collectChainedSignals(TrackGraph graph, SignalBoundary signal, boolean front) {
		HashMap<UUID, Boolean> map = new HashMap<>();
		walkSignals(graph, signal, front, pair -> {
			SignalBoundary boundary = pair.getSecond();
			map.put(boundary.id, !boundary.isPrimary(pair.getFirst()));
			return false;
		}, Predicates.alwaysFalse(), true);
		return map;
	}

	public static void walkSignals(TrackGraph graph, SignalBoundary signal, boolean front,
		Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback, Predicate<EdgeData> nonBoundaryCallback,
		boolean forCollection) {

		Couple<TrackNodeLocation> edgeLocation = signal.edgeLocation;
		Couple<TrackNode> startNodes = edgeLocation.map(graph::locateNode);
		Couple<TrackEdge> startEdges = startNodes.mapWithParams((l1, l2) -> graph.getConnectionsFrom(l1)
			.get(l2), startNodes.swap());

		TrackNode node1 = startNodes.get(front);
		TrackNode node2 = startNodes.get(!front);
		TrackEdge startEdge = startEdges.get(front);
		TrackEdge oppositeEdge = startEdges.get(!front);

		if (startEdge == null)
			return;

		if (!forCollection) {
			notifyTrains(graph, startEdge, oppositeEdge);
			startEdge.getEdgeData()
				.refreshIntersectingSignalGroups(graph);
			Create.RAILWAYS.sync.edgeDataChanged(graph, node1, node2, startEdge, oppositeEdge);
		}

		// Check for signal on the same edge
		SignalBoundary immediateBoundary = startEdge.getEdgeData()
			.next(EdgePointType.SIGNAL, signal.getLocationOn(startEdge));
		if (immediateBoundary != null) {
			if (boundaryCallback.test(Pair.of(node1, immediateBoundary)))
				startEdge.getEdgeData()
					.refreshIntersectingSignalGroups(graph);
			return;
		}

		// Search for any connected signals
		List<Couple<TrackNode>> frontier = new ArrayList<>();
		frontier.add(Couple.create(node2, node1));
		walkSignals(graph, frontier, boundaryCallback, nonBoundaryCallback, forCollection);
	}

	private static void walkSignals(TrackGraph graph, List<Couple<TrackNode>> frontier,
		Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback, Predicate<EdgeData> nonBoundaryCallback,
		boolean forCollection) {
		Set<TrackEdge> visited = new HashSet<>();
		while (!frontier.isEmpty()) {
			Couple<TrackNode> couple = frontier.remove(0);
			TrackNode currentNode = couple.getFirst();
			TrackNode prevNode = couple.getSecond();

			EdgeWalk: for (Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(currentNode)
				.entrySet()) {
				TrackNode nextNode = entry.getKey();
				TrackEdge edge = entry.getValue();

				if (nextNode == prevNode)
					continue;

				// already checked this edge
				if (!visited.add(edge))
					continue;

				// chain signal: check if reachable
				if (forCollection && !graph.getConnectionsFrom(prevNode)
					.get(currentNode)
					.canTravelTo(edge))
					continue;

				TrackEdge oppositeEdge = graph.getConnectionsFrom(nextNode)
					.get(currentNode);
				visited.add(oppositeEdge);

				for (boolean flip : Iterate.falseAndTrue) {
					TrackEdge currentEdge = flip ? oppositeEdge : edge;
					EdgeData signalData = currentEdge.getEdgeData();

					// no boundary- update group of edge
					if (!signalData.hasSignalBoundaries()) {
						if (nonBoundaryCallback.test(signalData)) {
							notifyTrains(graph, currentEdge);
							Create.RAILWAYS.sync.edgeDataChanged(graph, currentNode, nextNode, edge, oppositeEdge);
						}
						continue;
					}

					// other/own boundary found
					SignalBoundary nextBoundary = signalData.next(EdgePointType.SIGNAL, 0);
					if (nextBoundary == null)
						continue;
					if (boundaryCallback.test(Pair.of(currentNode, nextBoundary))) {
						notifyTrains(graph, edge, oppositeEdge);
						currentEdge.getEdgeData()
							.refreshIntersectingSignalGroups(graph);
						Create.RAILWAYS.sync.edgeDataChanged(graph, currentNode, nextNode, edge, oppositeEdge);
					}
					continue EdgeWalk;
				}

				frontier.add(Couple.create(nextNode, currentNode));
			}
		}
	}

	public static void notifyTrains(TrackGraph graph, TrackEdge... edges) {
		for (TrackEdge trackEdge : edges) {
			for (Train train : Create.RAILWAYS.trains.values()) {
				if (train.graph != graph)
					continue;
				if (train.updateSignalBlocks)
					continue;
				train.forEachTravellingPoint(tp -> {
					if (tp.edge == trackEdge)
						train.updateSignalBlocks = true;
				});
			}
		}
	}

}
