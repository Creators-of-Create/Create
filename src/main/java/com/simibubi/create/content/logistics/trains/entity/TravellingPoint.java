package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.management.GraphLocation;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.signal.EdgeData;
import com.simibubi.create.content.logistics.trains.management.signal.SignalBoundary;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.world.phys.Vec3;

public class TravellingPoint {

	public TrackNode node1, node2;
	public TrackEdge edge;
	public double position;
	public boolean blocked;

	public static enum SteerDirection {
		NONE(0), LEFT(-1), RIGHT(1);

		float targetDot;

		private SteerDirection(float targetDot) {
			this.targetDot = targetDot;
		}
	}

	public static interface ITrackSelector
		extends BiFunction<TrackGraph, Pair<Boolean, List<Entry<TrackNode, TrackEdge>>>, Entry<TrackNode, TrackEdge>> {
	};

	public static interface ISignalBoundaryListener extends BiConsumer<Double, Pair<SignalBoundary, Couple<UUID>>> {
	};

	public TravellingPoint() {}

	public TravellingPoint(TrackNode node1, TrackNode node2, TrackEdge edge, double position) {
		this.node1 = node1;
		this.node2 = node2;
		this.edge = edge;
		this.position = position;
	}

	public ISignalBoundaryListener ignoreSignals() {
		return (d, c) -> {
		};
	}

	public ITrackSelector random() {
		return (graph, pair) -> pair.getSecond()
			.get(Create.RANDOM.nextInt(pair.getSecond()
				.size()));
	}

	public ITrackSelector follow(TravellingPoint other) {
		return (graph, pair) -> {
			List<Entry<TrackNode, TrackEdge>> validTargets = pair.getSecond();
			boolean forward = pair.getFirst();
			TrackNode target = forward ? other.node1 : other.node2;
			TrackNode secondary = forward ? other.node2 : other.node1;

			for (Entry<TrackNode, TrackEdge> entry : validTargets)
				if (entry.getKey() == target || entry.getKey() == secondary)
					return entry;

			Vector<List<Entry<TrackNode, TrackEdge>>> frontiers = new Vector<>(validTargets.size());
			Vector<Set<TrackEdge>> visiteds = new Vector<>(validTargets.size());

			for (int j = 0; j < validTargets.size(); j++) {
				ArrayList<Entry<TrackNode, TrackEdge>> e = new ArrayList<>();
				Entry<TrackNode, TrackEdge> entry = validTargets.get(j);
				e.add(entry);
				frontiers.add(e);
				HashSet<TrackEdge> e2 = new HashSet<>();
				e2.add(entry.getValue());
				visiteds.add(e2);
			}

			for (int i = 0; i < 20; i++) {
				for (int j = 0; j < validTargets.size(); j++) {
					Entry<TrackNode, TrackEdge> entry = validTargets.get(j);
					List<Entry<TrackNode, TrackEdge>> frontier = frontiers.get(j);
					if (frontier.isEmpty())
						continue;

					Entry<TrackNode, TrackEdge> currentEntry = frontier.remove(0);
					for (Entry<TrackNode, TrackEdge> nextEntry : graph.getConnectionsFrom(currentEntry.getKey())
						.entrySet()) {
						TrackEdge nextEdge = nextEntry.getValue();
						if (!visiteds.get(j)
							.add(nextEdge))
							continue;

						TrackNode nextNode = nextEntry.getKey();
						if (nextNode == target)
							return entry;

						frontier.add(nextEntry);
					}
				}
			}

			Create.LOGGER.warn("Couldn't find follow target, choosing first");
			return validTargets.get(0);
		};
	}

	public ITrackSelector steer(SteerDirection direction, Vec3 upNormal) {
		return (graph, pair) -> {
			List<Entry<TrackNode, TrackEdge>> validTargets = pair.getSecond();
			double closest = Double.MAX_VALUE;
			Entry<TrackNode, TrackEdge> best = null;

			for (Entry<TrackNode, TrackEdge> entry : validTargets) {
				Vec3 trajectory = edge.getDirection(node1, node2, false);
				Vec3 entryTrajectory = entry.getValue()
					.getDirection(node2, entry.getKey(), true);
				Vec3 normal = trajectory.cross(upNormal);
				double dot = normal.dot(entryTrajectory);
				double diff = Math.abs(direction.targetDot - dot);
				if (diff > closest)
					continue;

				closest = diff;
				best = entry;
			}

			if (best == null) {
				Create.LOGGER.warn("Couldn't find steer target, choosing first");
				return validTargets.get(0);
			}

			return best;
		};
	}

	public double travel(TrackGraph graph, double distance, ITrackSelector trackSelector,
		ISignalBoundaryListener signalListener) {
		blocked = false;
		double edgeLength = edge.getLength(node1, node2);
		if (distance == 0)
			return 0;

		double prevPos = position;
		double traveled = distance;
		double currentT = position / edgeLength;
		double incrementT = edge.incrementT(node1, node2, currentT, distance);
		position = incrementT * edgeLength;
		List<Entry<TrackNode, TrackEdge>> validTargets = new ArrayList<>();

		boolean forward = distance > 0;
		double collectedDistance = forward ? -prevPos : -edgeLength + prevPos;
		edgeTraversedFrom(graph, forward, signalListener, prevPos, collectedDistance);

		if (forward) {
			// Moving forward
			while (position > edgeLength) {
				validTargets.clear();

				for (Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(node2)
					.entrySet()) {
					TrackNode newNode = entry.getKey();
					if (newNode == node1)
						continue;

					TrackEdge newEdge = entry.getValue();
					Vec3 currentDirection = edge.getDirection(node1, node2, false);
					Vec3 newDirection = newEdge.getDirection(node2, newNode, true);
					if (currentDirection.dot(newDirection) < 3 / 4f)
						continue;

					validTargets.add(entry);
				}

				if (validTargets.isEmpty()) {
					traveled -= position - edgeLength;
					position = edgeLength;
					blocked = true;
					break;
				}

				Entry<TrackNode, TrackEdge> entry = validTargets.size() == 1 ? validTargets.get(0)
					: trackSelector.apply(graph, Pair.of(true, validTargets));

				node1 = node2;
				node2 = entry.getKey();
				edge = entry.getValue();
				position -= edgeLength;

				collectedDistance += edgeLength;
				edgeTraversedFrom(graph, forward, signalListener, 0, collectedDistance);
				prevPos = 0;

				edgeLength = edge.getLength(node1, node2);
			}

		} else {
			// Moving backwards
			while (position < 0) {
				validTargets.clear();

				for (Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(node1)
					.entrySet()) {
					TrackNode newNode = entry.getKey();
					if (newNode == node2)
						continue;

					TrackEdge newEdge = graph.getConnectionsFrom(newNode)
						.get(node1);
					Vec3 currentDirection = edge.getDirection(node1, node2, true);
					Vec3 newDirection = newEdge.getDirection(newNode, node1, false);
					if (currentDirection.dot(newDirection) < 3 / 4f)
						continue;

					validTargets.add(entry);
				}

				if (validTargets.isEmpty()) {
					traveled -= position;
					position = 0;
					blocked = true;
					break;
				}

				Entry<TrackNode, TrackEdge> entry = validTargets.size() == 1 ? validTargets.get(0)
					: trackSelector.apply(graph, Pair.of(false, validTargets));

				node2 = node1;
				node1 = entry.getKey();
				edge = graph.getConnectionsFrom(node1)
					.get(node2);
				collectedDistance += edgeLength;
				edgeLength = edge.getLength(node1, node2);
				position += edgeLength;

				edgeTraversedFrom(graph, forward, signalListener, edgeLength, collectedDistance);
			}

		}

		return traveled;
	}

	private void edgeTraversedFrom(TrackGraph graph, boolean forward, ISignalBoundaryListener signalListener,
		double prevPos, double totalDistance) {
		EdgeData signalsOnEdge = edge.getEdgeData();
		if (!signalsOnEdge.hasSignalBoundaries())
			return;

		double from = forward ? prevPos : position;
		double to = forward ? position : prevPos;
		SignalBoundary nextBoundary = signalsOnEdge.next(EdgePointType.SIGNAL, node1, node2, edge, from);
		List<SignalBoundary> discoveredBoundaries = null;

		while (nextBoundary != null) {
			double d = nextBoundary.getLocationOn(node1, node2, edge);
			if (d > to)
				break;
			if (discoveredBoundaries == null)
				discoveredBoundaries = new ArrayList<>();
			discoveredBoundaries.add(nextBoundary);
			nextBoundary = signalsOnEdge.next(EdgePointType.SIGNAL, node1, node2, edge, d);
		}

		if (discoveredBoundaries == null)
			return;

		for (int i = 0; i < discoveredBoundaries.size(); i++) {
			int index = forward ? i : discoveredBoundaries.size() - i - 1;
			nextBoundary = discoveredBoundaries.get(index);
			double d = nextBoundary.getLocationOn(node1, node2, edge);
			if (!forward)
				d = edge.getLength(node1, node2) - d;
			Couple<UUID> nodes = Couple.create(nextBoundary.getGroup(node1), nextBoundary.getGroup(node2));
			signalListener.accept(totalDistance + d, Pair.of(nextBoundary, forward ? nodes : nodes.swap()));
		}
	}

	public void reverse(TrackGraph graph) {
		TrackNode n = node1;
		node1 = node2;
		node2 = n;
		position = edge.getLength(node1, node2) - position;
		edge = graph.getConnectionsFrom(node1)
			.get(node2);
	}

	public Vec3 getPosition() {
		double t = position / edge.getLength(node1, node2);
		return edge.getPosition(node1, node2, t)
			.add(edge.getNormal(node1, node2, t)
				.scale(1));
	}

	public void migrateTo(List<GraphLocation> locations) {
		GraphLocation location = locations.remove(0);
		TrackGraph graph = location.graph;
		node1 = graph.locateNode(location.edge.getFirst());
		node2 = graph.locateNode(location.edge.getSecond());
		position = location.position;
		edge = graph.getConnectionsFrom(node1)
			.get(node2);
	}

}
