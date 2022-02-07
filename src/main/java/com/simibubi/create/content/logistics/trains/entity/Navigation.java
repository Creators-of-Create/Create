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
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Navigation {

	Train train;
	public GlobalStation destination;
	public double distanceToDestination;
	public boolean destinationBehindTrain;
	List<TrackEdge> currentPath;

	public Navigation(Train train, TrackGraph graph) {
		this.train = train;
		currentPath = new ArrayList<>();
	}

	public void tick(Level level) {
		if (destination == null)
			return;

		if (!train.runtime.paused) {
			boolean frontDriver = train.hasForwardConductor();
			boolean backDriver = train.hasBackwardConductor();
			if (destinationBehindTrain && !backDriver) {
				if (frontDriver)
					train.status.missingBackwardsConductor();
				else
					train.status.missingConductor();
				cancelNavigation();
				return;
			}

			if (!destinationBehindTrain && !frontDriver) {
				train.status.missingConductor();
				cancelNavigation();
				return;
			}

			train.status.foundConductor();
		}

		destination.reserveFor(train);

		if (distanceToDestination < 1 / 32f) {
			distanceToDestination = 0;
			train.speed = 0;
			currentPath.clear();
			train.arriveAt(destination);
			destination = null;
			return;
		}

		float speedMod = destinationBehindTrain ? -1 : 1;
		train.currentlyBackwards = destinationBehindTrain;

		if (distanceToDestination - Math.abs(train.speed) < 1 / 32f) {
			train.speed = distanceToDestination * speedMod;
			return;
		}

		if (distanceToDestination < 10) {
			double target = Train.topSpeed * ((distanceToDestination) / 10);
			if (target < Math.abs(train.speed)) {
				train.speed += (target - Math.abs(train.speed)) * .5f * speedMod;
				return;
			}
		}

		double brakingDistance = (train.speed * train.speed) / (2 * Train.acceleration);
		train.targetSpeed = distanceToDestination > brakingDistance ? Train.topSpeed * speedMod : 0;
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
		boolean noneFound = pathTo.getFirst() == null;
		double distance = noneFound ? -1 : Math.abs(pathTo.getFirst());

		if (simulate)
			return distance;

		distanceToDestination = distance;
		currentPath = pathTo.getSecond();
		destinationBehindTrain = pathTo.getFirst() < 0;

		if (noneFound) {
			distanceToDestination = 0;
			if (this.destination != null)
				cancelNavigation();
			return -1;
		}

		if (this.destination == destination)
			return 0;
		
		if (!train.runtime.paused) {
			boolean frontDriver = train.hasForwardConductor();
			boolean backDriver = train.hasBackwardConductor();
			if (destinationBehindTrain && !backDriver) {
				if (frontDriver)
					train.status.missingBackwardsConductor();
				else
					train.status.missingConductor();
				return -1;
			}

			if (!destinationBehindTrain && !frontDriver) {
				if (backDriver)
					train.status.missingBackwardsConductor();
				else
					train.status.missingConductor();
				return -1;
			}

			train.status.foundConductor();
		}

		train.leaveStation();
		this.destination = destination;
		return distanceToDestination;
	}

	private Pair<Double, List<TrackEdge>> findPathTo(GlobalStation destination) {
		TrackGraph graph = train.graph;
		List<TrackEdge> path = new ArrayList<>();

		if (graph == null)
			return Pair.of(null, path);

		Couple<TrackNodeLocation> target = destination.edgeLocation;
		MutableObject<Pair<Double, List<TrackEdge>>> frontResult = new MutableObject<>(Pair.of(null, path));
		MutableObject<Pair<Double, List<TrackEdge>>> backResult = new MutableObject<>(Pair.of(null, path));

		for (boolean forward : Iterate.trueAndFalse) {
			if (this.destination == destination && destinationBehindTrain == forward)
				continue;

			List<TrackEdge> currentPath = new ArrayList<>();
			TravellingPoint initialPoint = forward ? train.carriages.get(0)
				.getLeadingPoint()
				: train.carriages.get(train.carriages.size() - 1)
					.getTrailingPoint();
			TrackEdge initialEdge = forward ? initialPoint.edge
				: graph.getConnectionsFrom(initialPoint.node2)
					.get(initialPoint.node1);

			search(Double.MAX_VALUE, forward, (reachedVia, poll) -> {

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
						currentPath.add(0, toReach);
					toReach = backTrack.getSecond();
					backTrack = reachedVia.get(backTrack.getSecond());
				}

				double position = edge.getLength(node1, node2) - destination.position;
				double distanceToDestination = distance - position;

				if (forward)
					frontResult.setValue(Pair.of(distanceToDestination, currentPath));
				else
					backResult.setValue(Pair.of(-distanceToDestination, currentPath));
				return true;
			});

			if (!train.doubleEnded)
				break;
		}

		Pair<Double, List<TrackEdge>> front = frontResult.getValue();
		Pair<Double, List<TrackEdge>> back = backResult.getValue();

		boolean frontEmpty = front.getFirst() == null;
		boolean backEmpty = back.getFirst() == null;
		if (backEmpty)
			return front;
		if (frontEmpty)
			return back;

		boolean canDriveForward = train.hasForwardConductor() || train.runtime.paused;
		boolean canDriveBackward = train.hasBackwardConductor() || train.runtime.paused;
		if (!canDriveBackward)
			return front;
		if (!canDriveForward)
			return back;

		boolean frontBetter = -back.getFirst() > front.getFirst();
		return frontBetter ? front : back;
	}

	public GlobalStation findNearestApproachable(boolean forward) {
		TrackGraph graph = train.graph;
		if (graph == null)
			return null;

		MutableObject<GlobalStation> result = new MutableObject<>(null);
		double minDistance = .75f * (train.speed * train.speed) / (2 * Train.acceleration);
		double maxDistance = Math.max(32, 1.5f * (train.speed * train.speed) / (2 * Train.acceleration));

		search(maxDistance, forward, (reachedVia, poll) -> {
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
		});

		return result.getValue();
	}

	public void search(double maxDistance, boolean forward,
		BiPredicate<Map<TrackEdge, Pair<Boolean, TrackEdge>>, Pair<Double, Pair<Couple<TrackNode>, TrackEdge>>> condition) {
		TrackGraph graph = train.graph;
		if (graph == null)
			return;

		TravellingPoint startingPoint = forward ? train.carriages.get(0)
			.getLeadingPoint()
			: train.carriages.get(train.carriages.size() - 1)
				.getTrailingPoint();

		Set<TrackEdge> visited = new HashSet<>();
		Map<TrackEdge, Pair<Boolean, TrackEdge>> reachedVia = new IdentityHashMap<>();
		PriorityQueue<Pair<Double, Pair<Couple<TrackNode>, TrackEdge>>> frontier =
			new PriorityQueue<>((p1, p2) -> Double.compare(p1.getFirst(), p2.getFirst()));

		TrackNode initialNode1 = forward ? startingPoint.node1 : startingPoint.node2;
		TrackNode initialNode2 = forward ? startingPoint.node2 : startingPoint.node1;
		TrackEdge initialEdge = graph.getConnectionsFrom(initialNode1)
			.get(initialNode2);
		double distanceToNode2 = forward ? initialEdge.getLength(initialNode1, initialNode2) - startingPoint.position
			: startingPoint.position;

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
