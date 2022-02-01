package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.content.logistics.trains.entity.MovingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Navigation {

	TrackGraph graph;
	Train train;

	public GlobalStation destination;
	public double distanceToDestination;

	List<TrackEdge> path;

	public Navigation(Train train, TrackGraph graph) {
		this.train = train;
		this.graph = graph;
		path = new ArrayList<>();
	}

	public void tick(Level level) {
		if (destination == null)
			return;

		destination.reserveFor(train);

		if (distanceToDestination < 1 / 32f) {
			distanceToDestination = 0;
			train.speed = 0;
			path.clear();
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

		if (Mth.equal(train.targetSpeed, train.speed))
			return;

		if (train.speed < train.targetSpeed)
			train.speed = Math.min(train.speed + Train.acceleration, train.targetSpeed);
		else if (train.speed > train.targetSpeed)
			train.speed = Math.max(train.speed - Train.acceleration, train.targetSpeed);

	}

	public boolean isActive() {
		return destination != null;
	}

	public ITrackSelector control(MovingPoint mp) {
		return list -> {
			if (!path.isEmpty()) {
				TrackEdge target = path.get(0);
				for (Entry<TrackNode, TrackEdge> entry : list) {
					if (entry.getValue() == target) {
						path.remove(0);
						return entry;
					}
				}
			}
			return list.get(0);
		};
	}

	public void cancelNavigation() {
		distanceToDestination = 0;
		path.clear();
		if (destination == null)
			return;
		destination.cancelReservation(train);
	}

	public void setDestination(GlobalStation destination) {
		findPathTo(destination);
		if (path.isEmpty())
			return;
		train.leave();
		this.destination = destination;
	}

	private void findPathTo(GlobalStation destination) {
		path.clear();
		this.distanceToDestination = 0;
		Couple<TrackNodeLocation> target = destination.edgeLocation;
		PriorityQueue<Pair<Double, Pair<Couple<TrackNode>, TrackEdge>>> frontier =
			new PriorityQueue<>((p1, p2) -> Double.compare(p1.getFirst(), p2.getFirst()));

		MovingPoint leadingPoint = train.carriages.get(0)
			.getLeadingPoint();
		Set<TrackEdge> visited = new HashSet<>();
		Map<TrackEdge, Pair<Boolean, TrackEdge>> reachedVia = new IdentityHashMap<>();

		TrackEdge initialEdge = leadingPoint.edge;
		TrackNode initialNode1 = leadingPoint.node1;
		TrackNode initialNode2 = leadingPoint.node2;
		double distanceToNode2 = initialEdge.getLength(initialNode1, initialNode2) - leadingPoint.position;
		frontier.add(Pair.of(distanceToNode2, Pair.of(Couple.create(initialNode1, initialNode2), initialEdge)));

		while (!frontier.isEmpty()) {
			Pair<Double, Pair<Couple<TrackNode>, TrackEdge>> poll = frontier.poll();
			double distance = poll.getFirst();
			Pair<Couple<TrackNode>, TrackEdge> currentEntry = poll.getSecond();
			List<Entry<TrackNode, TrackEdge>> validTargets = new ArrayList<>();
			TrackEdge edge = currentEntry.getSecond();
			TrackNode node1 = currentEntry.getFirst()
				.getFirst();
			TrackNode node2 = currentEntry.getFirst()
				.getSecond();

			TrackNodeLocation loc1 = node1.getLocation();
			TrackNodeLocation loc2 = node2.getLocation();
			boolean enteringBackward = loc2.equals(target.getFirst()) && loc1.equals(target.getSecond());
			boolean enteringForward = loc1.equals(target.getFirst()) && loc2.equals(target.getSecond());

			if (enteringForward || train.doubleEnded && enteringBackward) {
				Pair<Boolean, TrackEdge> backTrack = reachedVia.get(edge);
				TrackEdge toReach = edge;
				while (backTrack != null && toReach != initialEdge) {
					if (backTrack.getFirst())
						path.add(0, toReach);
					toReach = backTrack.getSecond();
					backTrack = reachedVia.get(backTrack.getSecond());
				}

				distanceToDestination = distance;
				double position = destination.position;
				if (enteringForward)
					position = edge.getLength(node1, node2) - position;
				else
					distanceToDestination += train.getTotalLength() + 2;
				distanceToDestination -= position;
				return;
			}

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
