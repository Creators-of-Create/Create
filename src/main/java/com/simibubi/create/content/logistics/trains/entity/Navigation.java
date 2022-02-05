package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Navigation {

	Train train;
	public GlobalStation destination;
	public double distanceToDestination;
	List<TrackEdge> currentPath;

	public Navigation(Train train, TrackGraph graph) {
		this.train = train;
		currentPath = new ArrayList<>();
	}

	public void tick(Level level) {
		if (destination == null)
			return;

		destination.reserveFor(train);

		if (distanceToDestination < 1 / 32f) {
			distanceToDestination = 0;
			train.speed = 0;
			currentPath.clear();
			train.arriveAt(destination);
			destination = null;
			return;
		}

		if (distanceToDestination - train.speed < 1 / 32f) {
			train.speed = distanceToDestination;
			return;
		}

		if (distanceToDestination < 10) {
			double target = Train.topSpeed * ((distanceToDestination) / 10);
			if (target < train.speed) {
				train.speed += (target - train.speed) * .5f;
				return;
			}
		}

		double brakingDistance = (train.speed * train.speed) / (2 * Train.acceleration);
		train.targetSpeed = distanceToDestination > brakingDistance ? Train.topSpeed : 0;
		train.approachTargetSpeed(1);
	}

	public boolean isActive() {
		return destination != null;
	}

	public ITrackSelector control(TravellingPoint mp) {
		if (destination == null)
			return mp.steer(train.manualSteer, new Vec3(0, 1, 0));
		return (graph, pair) -> {
			if (!currentPath.isEmpty()) {
				TrackEdge target = currentPath.get(0);
				for (Entry<TrackNode, TrackEdge> entry : pair.getSecond()) {
					if (entry.getValue() == target) {
						currentPath.remove(0);
						return entry;
					}
				}
			}
			return pair.getSecond()
				.get(0);
		};
	}

	public void cancelNavigation() {
		distanceToDestination = 0;
		currentPath.clear();
		if (destination == null)
			return;
		destination.cancelReservation(train);
		destination = null;
		train.runtime.transitInterrupted();
	}

	public double startNavigation(GlobalStation destination, boolean simulate) {
		Pair<Double, List<TrackEdge>> pathTo = findPathTo(destination);

		if (simulate)
			return pathTo.getFirst();

		distanceToDestination = pathTo.getFirst();
		currentPath = pathTo.getSecond();
		if (distanceToDestination == -1) {
			distanceToDestination = 0;
			if (this.destination != null)
				cancelNavigation();
			return -1;
		}

		if (this.destination == destination)
			return 0;

		train.leaveStation();
		this.destination = destination;
		return distanceToDestination;
	}

	private Pair<Double, List<TrackEdge>> findPathTo(GlobalStation destination) {
		TrackGraph graph = train.graph;
		List<TrackEdge> path = new ArrayList<>();

		if (graph == null)
			return Pair.of(-1d, path);

		Couple<TrackNodeLocation> target = destination.edgeLocation;
		TravellingPoint leadingPoint = train.carriages.get(0)
			.getLeadingPoint();
		TrackEdge initialEdge = leadingPoint.edge;

		MutableObject<Pair<Double, List<TrackEdge>>> result = new MutableObject<>(Pair.of(-1d, path));

		search((reachedVia, poll) -> {
			double distance = poll.getFirst();
			Pair<Couple<TrackNode>, TrackEdge> currentEntry = poll.getSecond();
			TrackEdge edge = currentEntry.getSecond();
			TrackNode node1 = currentEntry.getFirst()
				.getFirst();
			TrackNode node2 = currentEntry.getFirst()
				.getSecond();

			TrackNodeLocation loc1 = node1.getLocation();
			TrackNodeLocation loc2 = node2.getLocation();
			if (!loc1.equals(target.getFirst()) || !loc2.equals(target.getSecond()))
				return false;

			Pair<Boolean, TrackEdge> backTrack = reachedVia.get(edge);
			TrackEdge toReach = edge;
			while (backTrack != null && toReach != initialEdge) {
				if (backTrack.getFirst())
					path.add(0, toReach);
				toReach = backTrack.getSecond();
				backTrack = reachedVia.get(backTrack.getSecond());
			}

			double distanceToDestination = distance;
			double position = edge.getLength(node1, node2) - destination.position;
			distanceToDestination -= position;
			result.setValue(Pair.of(distanceToDestination, path));
			return true;
		}, Double.MAX_VALUE);

		return result.getValue();
	}

	public GlobalStation findNearestApproachable() {
		TrackGraph graph = train.graph;
		if (graph == null)
			return null;

		MutableObject<GlobalStation> result = new MutableObject<>(null);
		double minDistance = .75f * (train.speed * train.speed) / (2 * Train.acceleration);
		double maxDistance = Math.max(32, 1.5f * (train.speed * train.speed) / (2 * Train.acceleration));

		search((reachedVia, poll) -> {
			double distance = poll.getFirst();
			if (distance < minDistance)
				return false;

			Pair<Couple<TrackNode>, TrackEdge> currentEntry = poll.getSecond();
			TrackEdge edge = currentEntry.getSecond();
			TrackNode node1 = currentEntry.getFirst()
				.getFirst();
			TrackNode node2 = currentEntry.getFirst()
				.getSecond();

			for (GlobalStation globalStation : graph.getStations()) {
				Couple<TrackNodeLocation> target = globalStation.edgeLocation;
				TrackNodeLocation loc1 = node1.getLocation();
				TrackNodeLocation loc2 = node2.getLocation();
				if (!loc1.equals(target.getFirst()) || !loc2.equals(target.getSecond()))
					continue;
				double position = edge.getLength(node1, node2) - globalStation.position;
				if (distance - position < minDistance)
					continue;
				result.setValue(globalStation);
				return true;
			}

			return false;
		}, maxDistance);

		return result.getValue();
	}

	public void search(
		BiPredicate<Map<TrackEdge, Pair<Boolean, TrackEdge>>, Pair<Double, Pair<Couple<TrackNode>, TrackEdge>>> condition,
		double maxDistance) {
		TrackGraph graph = train.graph;
		if (graph == null)
			return;

		TravellingPoint leadingPoint = train.carriages.get(0)
			.getLeadingPoint();
		Set<TrackEdge> visited = new HashSet<>();
		Map<TrackEdge, Pair<Boolean, TrackEdge>> reachedVia = new IdentityHashMap<>();
		PriorityQueue<Pair<Double, Pair<Couple<TrackNode>, TrackEdge>>> frontier =
			new PriorityQueue<>((p1, p2) -> Double.compare(p1.getFirst(), p2.getFirst()));

		TrackEdge initialEdge = leadingPoint.edge;
		TrackNode initialNode1 = leadingPoint.node1;
		TrackNode initialNode2 = leadingPoint.node2;
		double distanceToNode2 = initialEdge.getLength(initialNode1, initialNode2) - leadingPoint.position;
		frontier.add(Pair.of(distanceToNode2, Pair.of(Couple.create(initialNode1, initialNode2), initialEdge)));

		while (!frontier.isEmpty()) {
			Pair<Double, Pair<Couple<TrackNode>, TrackEdge>> poll = frontier.poll();
			double distance = poll.getFirst();
			if (distance > maxDistance)
				continue;

			Pair<Couple<TrackNode>, TrackEdge> currentEntry = poll.getSecond();
			TrackEdge edge = currentEntry.getSecond();
			TrackNode node1 = currentEntry.getFirst()
				.getFirst();
			TrackNode node2 = currentEntry.getFirst()
				.getSecond();
			if (condition.test(reachedVia, poll))
				return;

			List<Entry<TrackNode, TrackEdge>> validTargets = new ArrayList<>();
			for (Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(node2)
				.entrySet()) {
				TrackNode newNode = entry.getKey();
				TrackEdge newEdge = entry.getValue();
				Vec3 currentDirection = edge.getDirection(node1, node2, false);
				Vec3 newDirection = newEdge.getDirection(node2, newNode, true);
				if (currentDirection.dot(newDirection) < 0)
					continue;
				if (!visited.add(entry.getValue()))
					continue;
				validTargets.add(entry);
			}

			if (validTargets.isEmpty())
				continue;

			for (Entry<TrackNode, TrackEdge> entry : validTargets) {
				TrackNode newNode = entry.getKey();
				TrackEdge newEdge = entry.getValue();
				reachedVia.put(newEdge, Pair.of(validTargets.size() > 1, edge));
				frontier.add(Pair.of(newEdge.getLength(node2, newNode) + distance,
					Pair.of(Couple.create(node2, newNode), newEdge)));
			}
		}
	}

}
