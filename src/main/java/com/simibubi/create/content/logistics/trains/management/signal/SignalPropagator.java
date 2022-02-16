package com.simibubi.create.content.logistics.trains.management.signal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

public class SignalPropagator {

	public static <T extends TrackEdgePoint> void onEdgePointAdded(TrackGraph graph, T point, Class<T> type) {
		Couple<TrackNodeLocation> edgeLocation = point.edgeLocation;
		Couple<TrackNode> startNodes = edgeLocation.map(graph::locateNode);
		Couple<TrackEdge> startEdges = startNodes.mapWithParams((l1, l2) -> graph.getConnectionsFrom(l1)
			.get(l2), startNodes.swap());

		for (boolean front : Iterate.trueAndFalse) {
			TrackNode node1 = startNodes.get(front);
			TrackNode node2 = startNodes.get(!front);
			TrackEdge startEdge = startEdges.get(front);
			startEdge.getEdgeData()
				.addPoint(node1, node2, startEdge, point, type);
		}
	}

	public static <T extends TrackEdgePoint> void onEdgePointRemoved(TrackGraph graph, T point, Class<T> type) {
		Couple<TrackNodeLocation> edgeLocation = point.edgeLocation;
		Couple<TrackNode> startNodes = edgeLocation.map(graph::locateNode);
		startNodes.forEachWithParams((l1, l2) -> {
			TrackEdge trackEdge = graph.getConnectionsFrom(l1)
				.get(l2);
			trackEdge.getEdgeData()
				.removePoint(l1, l2, trackEdge, point);
		}, startNodes.swap());
	}

	public static void onSignalRemoved(TrackGraph graph, SignalBoundary signal) {
		signal.sidesToUpdate.map($ -> false);
		for (boolean front : Iterate.trueAndFalse) {
			if (signal.sidesToUpdate.get(front))
				continue;
			Create.RAILWAYS.signalEdgeGroups.remove(signal.groups.get(front));
			walkSignals(graph, signal, front, pair -> {
				TrackNode node1 = pair.getFirst();
				SignalBoundary boundary = pair.getSecond();
				boundary.queueUpdate(node1);
				return false;
			}, Predicates.alwaysFalse());
		}

		onEdgePointRemoved(graph, signal, SignalBoundary.class);
	}

	public static void notifySignalsOfNewNode(TrackGraph graph, TrackNode node) {
		List<Couple<TrackNode>> frontier = new ArrayList<>();
		frontier.add(Couple.create(node, null));
		walkSignals(graph, frontier, pair -> {
			TrackNode node1 = pair.getFirst();
			SignalBoundary boundary = pair.getSecond();
			boundary.queueUpdate(node1);
			return false;
		}, Predicates.alwaysFalse());
	}

	public static void propagateSignalGroup(TrackGraph graph, SignalBoundary signal, boolean front) {
		Map<UUID, SignalEdgeGroup> globalGroups = Create.RAILWAYS.signalEdgeGroups;
		SignalEdgeGroup group = new SignalEdgeGroup(UUID.randomUUID());
		UUID groupId = group.id;
		globalGroups.put(groupId, group);
		signal.groups.set(front, groupId);

		walkSignals(graph, signal, front, pair -> {
			TrackNode node1 = pair.getFirst();
			SignalBoundary boundary = pair.getSecond();
			UUID currentGroup = boundary.getGroup(node1);
			if (currentGroup != null)
				globalGroups.remove(currentGroup);
			boundary.setGroup(node1, groupId);
			return true;

		}, signalData -> {
			if (signalData.singleSignalGroup != null)
				globalGroups.remove(signalData.singleSignalGroup);
			signalData.singleSignalGroup = groupId;
			return true;

		});
	}

	public static void walkSignals(TrackGraph graph, SignalBoundary signal, boolean front,
		Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback, Predicate<EdgeData> nonBoundaryCallback) {

		Couple<TrackNodeLocation> edgeLocation = signal.edgeLocation;
		Couple<TrackNode> startNodes = edgeLocation.map(graph::locateNode);
		Couple<TrackEdge> startEdges = startNodes.mapWithParams((l1, l2) -> graph.getConnectionsFrom(l1)
			.get(l2), startNodes.swap());

		TrackNode node1 = startNodes.get(front);
		TrackNode node2 = startNodes.get(!front);
		TrackEdge startEdge = startEdges.get(front);

		if (startEdge == null)
			return;

		// Check for signal on the same edge
		SignalBoundary immediateBoundary = startEdge.getEdgeData()
			.nextBoundary(node1, node2, startEdge, signal.getLocationOn(node1, node2, startEdge));
		if (immediateBoundary != null) {
			if (boundaryCallback.test(Pair.of(node1, immediateBoundary)))
				notifyTrains(graph, startEdge, startEdges.get(!front));
			return;
		}

		// Search for any connected signals
		List<Couple<TrackNode>> frontier = new ArrayList<>();
		frontier.add(Couple.create(node2, node1));
		walkSignals(graph, frontier, boundaryCallback, nonBoundaryCallback);
	}

	private static void walkSignals(TrackGraph graph, List<Couple<TrackNode>> frontier,
		Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback, Predicate<EdgeData> nonBoundaryCallback) {
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

				TrackEdge oppositeEdge = graph.getConnectionsFrom(nextNode)
					.get(currentNode);
				visited.add(oppositeEdge);

				for (boolean flip : Iterate.falseAndTrue) {
					TrackEdge currentEdge = flip ? oppositeEdge : edge;
					EdgeData signalData = currentEdge.getEdgeData();

					// no boundary- update group of edge
					if (!signalData.hasBoundaries()) {
						if (nonBoundaryCallback.test(signalData))
							notifyTrains(graph, currentEdge);
						continue;
					}

					// other/own boundary found
					SignalBoundary nextBoundary = signalData.nextBoundary(currentNode, nextNode, currentEdge, 0);
					if (boundaryCallback.test(Pair.of(currentNode, nextBoundary)))
						notifyTrains(graph, edge, oppositeEdge);
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
