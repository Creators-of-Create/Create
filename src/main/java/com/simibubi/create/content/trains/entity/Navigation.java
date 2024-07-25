package com.simibubi.create.content.trains.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.content.trains.graph.DiscoveredPath;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.signal.SignalBlock.SignalType;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Navigation {

	public Train train;

	public GlobalStation destination;
	public double distanceToDestination;
	public double distanceStartedAt;
	public boolean destinationBehindTrain;
	public boolean announceArrival;
	List<Couple<TrackNode>> currentPath;

	private TravellingPoint signalScout;
	public Pair<UUID, Boolean> waitingForSignal;
	private Map<UUID, Pair<SignalBoundary, Boolean>> waitingForChainedGroups;
	public double distanceToSignal;
	public int ticksWaitingForSignal;

	public Navigation(Train train) {
		this.train = train;
		currentPath = new ArrayList<>();
		signalScout = new TravellingPoint();
		waitingForChainedGroups = new HashMap<>();
	}

	public void tick(Level level) {
		if (destination == null)
			return;

		if (!train.runtime.paused) {
			boolean frontDriver = train.hasForwardConductor();
			boolean backDriver = train.hasBackwardConductor();
			if (destinationBehindTrain && !backDriver) {
				if (frontDriver)
					train.status.missingCorrectConductor();
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

		double acceleration = train.acceleration();
		double brakingDistance = (train.speed * train.speed) / (2 * acceleration);
		double speedMod = destinationBehindTrain ? -1 : 1;
		double preDepartureLookAhead = train.getCurrentStation() != null ? 4.5 : 0;
		double distanceToNextCurve = -1;

		// Signals
		if (train.graph != null) {
			if (waitingForSignal != null && currentSignalResolved()) {
				UUID signalId = waitingForSignal.getFirst();
				SignalBoundary signal = train.graph.getPoint(EdgePointType.SIGNAL, signalId);
				if (signal != null && signal.types.get(waitingForSignal.getSecond()) == SignalType.CROSS_SIGNAL)
					waitingForChainedGroups.clear();
				waitingForSignal = null;
			}

			TravellingPoint leadingPoint = !destinationBehindTrain ? train.carriages.get(0)
				.getLeadingPoint()
				: train.carriages.get(train.carriages.size() - 1)
					.getTrailingPoint();

			if (waitingForSignal == null) {
				distanceToSignal = Double.MAX_VALUE;
				ticksWaitingForSignal = 0;
			}

			if (distanceToSignal > 1 / 16f) {
				MutableDouble curveDistanceTracker = new MutableDouble(-1);

				signalScout.node1 = leadingPoint.node1;
				signalScout.node2 = leadingPoint.node2;
				signalScout.edge = leadingPoint.edge;
				signalScout.position = leadingPoint.position;

				double brakingDistanceNoFlicker = brakingDistance + 3 - (brakingDistance % 3);
				double scanDistance = Mth.clamp(brakingDistanceNoFlicker, preDepartureLookAhead, distanceToDestination);

				MutableDouble crossSignalDistanceTracker = new MutableDouble(-1);
				MutableObject<Pair<UUID, Boolean>> trackingCrossSignal = new MutableObject<>(null);
				waitingForChainedGroups.clear();

				// Adding 50 to the distance due to unresolved inaccuracies in
				// TravellingPoint::travel
				signalScout.travel(train.graph, (distanceToDestination + 50) * speedMod, controlSignalScout(),
					(distance, couple) -> {
						// > scanDistance and not following down a cross signal
						boolean crossSignalTracked = trackingCrossSignal.getValue() != null;
						if (!crossSignalTracked && distance > scanDistance)
							return true;

						Couple<TrackNode> nodes = couple.getSecond();
						TrackEdgePoint boundary = couple.getFirst();
						if (boundary == destination && ((GlobalStation) boundary).canApproachFrom(nodes.getSecond()))
							return true;
						if (!(boundary instanceof SignalBoundary signal))
							return false;

						UUID entering = signal.getGroup(nodes.getSecond());
						SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(entering);
						if (signalEdgeGroup == null)
							return false;

						boolean primary = entering.equals(signal.groups.getFirst());
						boolean crossSignal = signal.types.get(primary) == SignalType.CROSS_SIGNAL;
						boolean occupied = !train.manualTick
							&& (signal.isForcedRed(nodes.getSecond()) || signalEdgeGroup.isOccupiedUnless(train));

						if (!crossSignalTracked) {
							if (crossSignal) { // Now entering cross signal path
								trackingCrossSignal.setValue(Pair.of(boundary.id, primary));
								crossSignalDistanceTracker.setValue(distance);
								waitingForChainedGroups.put(entering, Pair.of(signal, primary));
							}
							if (occupied) { // Section is occupied
								waitingForSignal = Pair.of(boundary.id, primary);
								distanceToSignal = distance;
								if (!crossSignal)
									return true; // Standard entry signal, do not collect any further segments
							}
							if (!occupied && !crossSignal && distance < distanceToSignal + .25
								&& distance < brakingDistanceNoFlicker)
								signalEdgeGroup.reserved = signal; // Reserve group for traversal
							return false;
						}

						if (crossSignalTracked) {
							waitingForChainedGroups.put(entering, Pair.of(signal, primary)); // Add group to chain
							if (occupied) { // Section is occupied, but wait at the cross signal that started the chain
								waitingForSignal = trackingCrossSignal.getValue();
								distanceToSignal = crossSignalDistanceTracker.doubleValue();
								if (!crossSignal)
									return true; // Entry signals end a chain
							}
							if (!crossSignal) {
								if (distance < distanceToSignal + .25) {
									// Collect and reset the signal chain because none were blocked
									trackingCrossSignal.setValue(null);
									reserveChain();
									return false;
								} else
									return true; // End of a blocked signal chain
							}
						}

						return false;

					}, (distance, edge) -> {
						BezierConnection turn = edge.getTurn();
						double vDistance = Math.abs(turn.starts.getFirst().y - turn.starts.getSecond().y);

						// ignore turn if its a straight & mild slope
						if (turn != null && vDistance > 1 / 16f) {
							if (turn.axes.getFirst()
								.multiply(1, 0, 1)
								.distanceTo(turn.axes.getSecond()
									.multiply(1, 0, 1)
									.scale(-1)) < 1 / 64f
								&& vDistance / turn.getLength() < .225f)
								return;
						}

						float current = curveDistanceTracker.floatValue();
						if (current == -1 || distance < current)
							curveDistanceTracker.setValue(distance);
					});

				if (trackingCrossSignal.getValue() != null && waitingForSignal == null)
					reserveChain();

				distanceToNextCurve = curveDistanceTracker.floatValue();

			} else
				ticksWaitingForSignal++;
		}

		double targetDistance = waitingForSignal != null ? distanceToSignal : distanceToDestination;

		// always overshoot to ensure the travelling point crosses the target
		targetDistance += 0.25d;

		// dont leave until green light
		if (targetDistance > 1 / 32f && train.getCurrentStation() != null) {
			if (waitingForSignal != null && distanceToSignal < preDepartureLookAhead) {
				ticksWaitingForSignal++;
				return;
			}
			train.leaveStation();
		}

		train.currentlyBackwards = destinationBehindTrain;

		if (targetDistance < -10) {
			cancelNavigation();
			return;
		}

		if (targetDistance - Math.abs(train.speed) < 1 / 32f) {
			train.speed = Math.max(targetDistance, 1 / 32f) * speedMod;
			return;
		}

		train.burnFuel();

		double topSpeed = train.maxSpeed();

		if (targetDistance < 10) {
			double maxApproachSpeed = topSpeed * ((targetDistance) / 10);
			double speedRelativeToStation = train.speed * speedMod;
			
			if (speedRelativeToStation > maxApproachSpeed) {
				train.speed += (maxApproachSpeed - Math.abs(train.speed)) * .5f * speedMod;
				return;
			}
		}

		topSpeed *= train.throttle;
		double turnTopSpeed = Math.min(topSpeed, train.maxTurnSpeed());

		double targetSpeed = targetDistance > brakingDistance ? topSpeed * speedMod : 0;

		if (distanceToNextCurve != -1) {
			double slowingDistance = brakingDistance - (turnTopSpeed * turnTopSpeed) / (2 * acceleration);
			double targetTurnSpeed =
				distanceToNextCurve > slowingDistance ? topSpeed * speedMod : turnTopSpeed * speedMod;
			if (Math.abs(targetTurnSpeed) < Math.abs(targetSpeed))
				targetSpeed = targetTurnSpeed;
		}

		train.targetSpeed = targetSpeed;
		train.approachTargetSpeed(1);
	}

	private void reserveChain() {
		train.reservedSignalBlocks.addAll(waitingForChainedGroups.keySet());
		waitingForChainedGroups.forEach((groupId, boundary) -> {
			SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(groupId);
			if (signalEdgeGroup != null)
				signalEdgeGroup.reserved = boundary.getFirst();
		});
		waitingForChainedGroups.clear();
	}

	private boolean currentSignalResolved() {
		if (train.manualTick)
			return true;
		if (distanceToDestination < .5f)
			return true;
		SignalBoundary signal = train.graph.getPoint(EdgePointType.SIGNAL, waitingForSignal.getFirst());
		if (signal == null)
			return true;

		// Cross Signal
		if (signal.types.get(waitingForSignal.getSecond()) == SignalType.CROSS_SIGNAL) {
			for (Entry<UUID, Pair<SignalBoundary, Boolean>> entry : waitingForChainedGroups.entrySet()) {
				Pair<SignalBoundary, Boolean> boundary = entry.getValue();
				SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(entry.getKey());
				if (signalEdgeGroup == null) { // Migration, re-initialize chain
					waitingForSignal.setFirst(null);
					return true;
				}
				if (boundary.getFirst()
					.isForcedRed(boundary.getSecond())) {
					train.reservedSignalBlocks.clear();
					return false;
				}
				if (signalEdgeGroup.isOccupiedUnless(train))
					return false;
			}
			return true;
		}

		// Entry Signal
		UUID groupId = signal.groups.get(waitingForSignal.getSecond());
		if (groupId == null)
			return true;
		SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(groupId);
		if (signalEdgeGroup == null)
			return true;
		if (!signalEdgeGroup.isOccupiedUnless(train))
			return true;
		return false;
	}

	public boolean isActive() {
		return destination != null;
	}

	public ITrackSelector control(TravellingPoint mp) {
		if (destination == null)
			return mp.steer(train.manualSteer, new Vec3(0, 1, 0));
		return (graph, pair) -> navigateOptions(currentPath, graph, pair.getSecond());
	}

	public ITrackSelector controlSignalScout() {
		if (destination == null)
			return signalScout.steer(train.manualSteer, new Vec3(0, 1, 0));
		List<Couple<TrackNode>> pathCopy = new ArrayList<>(currentPath);
		return (graph, pair) -> navigateOptions(pathCopy, graph, pair.getSecond());
	}

	private Entry<TrackNode, TrackEdge> navigateOptions(List<Couple<TrackNode>> path, TrackGraph graph,
		List<Entry<TrackNode, TrackEdge>> options) {
		if (path.isEmpty())
			return options.get(0);
		Couple<TrackNode> nodes = path.get(0);
		TrackEdge targetEdge = graph.getConnection(nodes);
		for (Entry<TrackNode, TrackEdge> entry : options) {
			if (entry.getValue() != targetEdge)
				continue;
			path.remove(0);
			return entry;
		}
		return options.get(0);
	}

	public void cancelNavigation() {
		distanceToDestination = 0;
		currentPath.clear();
		if (destination == null)
			return;
		destination.cancelReservation(train);
		destination = null;
		train.runtime.transitInterrupted();
		train.reservedSignalBlocks.clear();
	}

	public double startNavigation(DiscoveredPath pathTo) {
		boolean noneFound = pathTo == null;
		double distance = noneFound ? -1 : Math.abs(pathTo.distance);
		double cost = noneFound ? -1 : pathTo.cost;

		distanceToDestination = distance;

		if (noneFound) {
			distanceToDestination = distanceStartedAt = 0;
			currentPath = new ArrayList<>();
			if (this.destination != null)
				cancelNavigation();
			return -1;
		}

		if (Math.abs(distanceToDestination) > 100)
			announceArrival = true;

		currentPath = pathTo.path;
		destinationBehindTrain = pathTo.distance < 0;
		train.reservedSignalBlocks.clear();
		train.navigation.waitingForSignal = null;

		if (this.destination == null)
			distanceStartedAt = distance;

		if (this.destination == pathTo.destination)
			return 0;

		if (!train.runtime.paused) {
			boolean frontDriver = train.hasForwardConductor();
			boolean backDriver = train.hasBackwardConductor();
			if (destinationBehindTrain && !backDriver) {
				if (frontDriver)
					train.status.missingCorrectConductor();
				else
					train.status.missingConductor();
				return -1;
			}

			if (!destinationBehindTrain && !frontDriver) {
				if (backDriver)
					train.status.missingCorrectConductor();
				else
					train.status.missingConductor();
				return -1;
			}

			train.status.foundConductor();
		}

		this.destination = pathTo.destination;
		return cost;
	}

	@Nullable
	public DiscoveredPath findPathTo(GlobalStation destination, double maxCost) {
		ArrayList<GlobalStation> destinations = new ArrayList<>();
		destinations.add(destination);
		return findPathTo(destinations, maxCost);
	}

	@Nullable
	public DiscoveredPath findPathTo(ArrayList<GlobalStation> destinations, double maxCost) {
		TrackGraph graph = train.graph;
		if (graph == null)
			return null;

		Couple<DiscoveredPath> results = Couple.create(null, null);
		for (boolean forward : Iterate.trueAndFalse) {

			// When updating destinations midtransit, avoid reversing out of path
			if (this.destination != null && destinationBehindTrain == forward)
				continue;

			TravellingPoint initialPoint = forward ? train.carriages.get(0)
				.getLeadingPoint()
				: train.carriages.get(train.carriages.size() - 1)
					.getTrailingPoint();
			TrackEdge initialEdge = forward ? initialPoint.edge
				: graph.getConnectionsFrom(initialPoint.node2)
					.get(initialPoint.node1);

			search(Double.MAX_VALUE, maxCost, forward, destinations, (distance, cost, reachedVia, currentEntry, globalStation) -> {
				for (GlobalStation destination : destinations){
					if (globalStation == destination) {
						TrackEdge edge = currentEntry.getSecond();
						TrackNode node1 = currentEntry.getFirst()
								.getFirst();
						TrackNode node2 = currentEntry.getFirst()
								.getSecond();

						List<Couple<TrackNode>> currentPath = new ArrayList<>();
						Pair<Boolean, Couple<TrackNode>> backTrack = reachedVia.get(edge);
						Couple<TrackNode> toReach = Couple.create(node1, node2);
						TrackEdge edgeReached = edge;
						while (backTrack != null) {
							if (edgeReached == initialEdge)
								break;
							if (backTrack.getFirst())
								currentPath.add(0, toReach);
							toReach = backTrack.getSecond();
							edgeReached = graph.getConnection(toReach);
							backTrack = reachedVia.get(edgeReached);
						}

						double position = edge.getLength() - destination.getLocationOn(edge);
						double distanceToDestination = distance - position;
						results.set(forward, new DiscoveredPath((forward ? 1 : -1) * distanceToDestination, cost, currentPath, destination));
						return true;
					}
				}
				return false;
			});
		}

		DiscoveredPath front = results.getFirst();
		DiscoveredPath back = results.getSecond();

		boolean frontEmpty = front == null;
		boolean backEmpty = back == null;
		boolean canDriveForward = train.hasForwardConductor() || train.runtime.paused;
		boolean canDriveBackward = train.doubleEnded && train.hasBackwardConductor() || train.runtime.paused;

		if (backEmpty || !canDriveBackward)
			return canDriveForward ? front : null;
		if (frontEmpty || !canDriveForward)
			return canDriveBackward ? back : null;

		boolean frontBetter = maxCost == -1 ? -back.distance > front.distance : back.cost > front.cost;
		return frontBetter ? front : back;
	}

	public GlobalStation findNearestApproachable(boolean forward) {
		TrackGraph graph = train.graph;
		if (graph == null)
			return null;

		MutableObject<GlobalStation> result = new MutableObject<>(null);
		double acceleration = train.acceleration();
		double minDistance = .75f * (train.speed * train.speed) / (2 * acceleration);
		double maxDistance = Math.max(32, 1.5f * (train.speed * train.speed) / (2 * acceleration));

		search(maxDistance, forward, null, (distance, cost, reachedVia, currentEntry, globalStation) -> {
			if (distance < minDistance)
				return false;

			TrackEdge edge = currentEntry.getSecond();
			double position = edge.getLength() - globalStation.getLocationOn(edge);
			if (distance - position < minDistance)
				return false;
			Train presentTrain = globalStation.getPresentTrain();
			if (presentTrain != null && presentTrain != train)
				return false;
			result.setValue(globalStation);
			return true;
		});

		return result.getValue();
	}

	public void search(double maxDistance, boolean forward, ArrayList<GlobalStation> destinations, StationTest stationTest) {
		search(maxDistance, -1, forward, destinations, stationTest);
	}

	public void search(double maxDistance, double maxCost, boolean forward, ArrayList<GlobalStation> destinations, StationTest stationTest) {
		TrackGraph graph = train.graph;
		if (graph == null)
			return;

		// Cache the list of track types that the train can travel on
		Set<TrackMaterial.TrackType> validTypes = new HashSet<>();
		for (int i = 0; i < train.carriages.size(); i++) {
			Carriage carriage = train.carriages.get(i);
			if (i == 0) {
				validTypes.addAll(carriage.leadingBogey().type.getValidPathfindingTypes(carriage.leadingBogey().getStyle()));
			} else {
				validTypes.retainAll(carriage.leadingBogey().type.getValidPathfindingTypes(carriage.leadingBogey().getStyle()));
			}
			if (carriage.isOnTwoBogeys())
				validTypes.retainAll(carriage.trailingBogey().type.getValidPathfindingTypes(carriage.trailingBogey().getStyle()));
		}
		if (validTypes.isEmpty()) // if there are no valid track types, a route can't be found
			return;

		Map<TrackEdge, Integer> penalties = new IdentityHashMap<>();
		boolean costRelevant = maxCost >= 0;
		if (costRelevant) {
			for (Train otherTrain : Create.RAILWAYS.trains.values()) {
				if (otherTrain.graph != graph)
					continue;
				if (otherTrain == train)
					continue;
				int navigationPenalty = otherTrain.getNavigationPenalty();
				otherTrain.getEndpointEdges()
					.forEach(nodes -> {
						if (nodes.either(Objects::isNull))
							return;
						for (boolean flip : Iterate.trueAndFalse) {
							TrackEdge e = graph.getConnection(flip ? nodes.swap() : nodes);
							if (e == null)
								continue;
							int existing = penalties.getOrDefault(e, 0);
							penalties.put(e, existing + navigationPenalty / 2);
						}
					});
			}
		}

		TravellingPoint startingPoint = forward ? train.carriages.get(0)
			.getLeadingPoint()
			: train.carriages.get(train.carriages.size() - 1)
				.getTrailingPoint();

		Set<TrackEdge> visited = new HashSet<>();
		Map<TrackEdge, Pair<Boolean, Couple<TrackNode>>> reachedVia = new IdentityHashMap<>();
		PriorityQueue<FrontierEntry> frontier = new PriorityQueue<>();

		TrackNode initialNode1 = forward ? startingPoint.node1 : startingPoint.node2;
		TrackNode initialNode2 = forward ? startingPoint.node2 : startingPoint.node1;
		TrackEdge initialEdge = graph.getConnectionsFrom(initialNode1)
			.get(initialNode2);
		if (initialEdge == null)
			return;

		double distanceToNode2 = forward ? initialEdge.getLength() - startingPoint.position : startingPoint.position;

		int signalWeight = Mth.clamp(ticksWaitingForSignal * 2, Train.Penalties.RED_SIGNAL, 200);

		// Apply penalties to initial edge
		int initialPenalty = 0;
		if (costRelevant)
			initialPenalty += penalties.getOrDefault(initialEdge, 0);

		EdgeData initialSignalData = initialEdge.getEdgeData();
		if (initialSignalData.hasPoints()) {
			for (TrackEdgePoint point : initialSignalData.getPoints()) {
				if (point.getLocationOn(initialEdge) < initialEdge.getLength() - distanceToNode2)
					continue;
				if (costRelevant && distanceToNode2 + initialPenalty > maxCost)
					return;
				if (!point.canNavigateVia(initialNode2))
					return;
				if (point instanceof SignalBoundary signal) {
					if (signal.isForcedRed(initialNode2)) {
						initialPenalty += Train.Penalties.REDSTONE_RED_SIGNAL;
						continue;
					}
					UUID group = signal.getGroup(initialNode2);
					if (group == null)
						continue;
					SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(group);
					if (signalEdgeGroup == null)
						continue;
					if (signalEdgeGroup.isOccupiedUnless(signal)) {
						initialPenalty += signalWeight;
						signalWeight /= 2;
					}
				}
				if (point instanceof GlobalStation station) {
					Train presentTrain = station.getPresentTrain();
					boolean isOwnStation = presentTrain == train;
					if (presentTrain != null && !isOwnStation)
						initialPenalty += Train.Penalties.STATION_WITH_TRAIN;
					if (station.canApproachFrom(initialNode2) && stationTest.test(distanceToNode2, distanceToNode2 + initialPenalty, reachedVia,
							Pair.of(Couple.create(initialNode1, initialNode2), initialEdge), station))
						return;
					if (!isOwnStation)
						initialPenalty += Train.Penalties.STATION;
				}
			}
		}

		if (costRelevant && distanceToNode2 + initialPenalty > maxCost)
			return;

		frontier.add(new FrontierEntry(distanceToNode2, initialPenalty, initialNode1, initialNode2, initialEdge));

		while (!frontier.isEmpty()) {
			FrontierEntry entry = frontier.poll();
			if (!visited.add(entry.edge))
				continue;

			double distance = entry.distance;
			int penalty = entry.penalty;

			if (distance > maxDistance)
				continue;

			TrackEdge edge = entry.edge;
			TrackNode node1 = entry.node1;
			TrackNode node2 = entry.node2;

			if (entry.hasDestination) {
				EdgeData signalData = edge.getEdgeData();
				if (signalData.hasPoints()) {
					for (TrackEdgePoint point : signalData.getPoints()) {
						if (point instanceof GlobalStation station) {
							if (station.canApproachFrom(node2) && stationTest.test(distance, penalty, reachedVia,
									Pair.of(Couple.create(node1, node2), edge), station))
								return;
						}
					}
				}
			}

			List<Entry<TrackNode, TrackEdge>> validTargets = new ArrayList<>();
			Map<TrackNode, TrackEdge> connectionsFrom = graph.getConnectionsFrom(node2);
			for (Entry<TrackNode, TrackEdge> connection : connectionsFrom.entrySet()) {
				TrackNode newNode = connection.getKey();
				if (newNode == node1)
					continue;
				if (edge.canTravelTo(connection.getValue()))
					validTargets.add(connection);
			}

			if (validTargets.isEmpty())
				continue;

			Search: for (Entry<TrackNode, TrackEdge> target : validTargets) {
				if (!validTypes.contains(target.getValue().getTrackMaterial().trackType))
					continue;
				TrackNode newNode = target.getKey();
				TrackEdge newEdge = target.getValue();
				int newPenalty = penalty;
				double edgeLength = newEdge.getLength();
				double newDistance = distance + edgeLength;

				if (costRelevant)
					newPenalty += penalties.getOrDefault(newEdge, 0);

				// Apply penalty to next connected edge
				boolean hasDestination = false;
				EdgeData signalData = newEdge.getEdgeData();
				if (signalData.hasPoints()) {
					for (TrackEdgePoint point : signalData.getPoints()) {
						if (node2 == initialNode1 && point.getLocationOn(newEdge) < edgeLength - distanceToNode2)
							continue;
						if (costRelevant && newDistance + newPenalty > maxCost)
							continue Search;
						if (!point.canNavigateVia(newNode))
							continue Search;
						if (point instanceof SignalBoundary signal) {
							if (signal.isForcedRed(newNode)) {
								newPenalty += Train.Penalties.REDSTONE_RED_SIGNAL;
								continue;
							}
							UUID group = signal.getGroup(newNode);
							if (group == null)
								continue;
							SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(group);
							if (signalEdgeGroup == null)
								continue;
							if (signalEdgeGroup.isOccupiedUnless(signal)) {
								newPenalty += signalWeight;
								signalWeight /= 2;
							}
						}
						if (point instanceof GlobalStation station) {
							Train presentTrain = station.getPresentTrain();
							boolean isOwnStation = presentTrain == train;
							if (presentTrain != null && !isOwnStation)
								newPenalty += Train.Penalties.STATION_WITH_TRAIN;
							if (station.canApproachFrom(newNode) && stationTest.test(newDistance, newDistance + newPenalty, reachedVia,
									Pair.of(Couple.create(node2, newNode), newEdge), station)) {
								hasDestination = true;
								continue;
							}
							if (!isOwnStation)
								newPenalty += Train.Penalties.STATION;
						}
					}
				}

				if (costRelevant && newDistance + newPenalty > maxCost)
					continue;

				double remainingDist = 0;
				// Calculate remaining distance estimator for next connected edge
				if (destinations != null && !destinations.isEmpty()) {
					remainingDist = Double.MAX_VALUE;
					Vec3 newNodePosition = newNode.getLocation().getLocation();
					for (GlobalStation destination : destinations) {
						TrackNodeLocation destinationNode = destination.edgeLocation.getFirst();
						double dMin = Math.abs(newNodePosition.x - destinationNode.getLocation().x);
						double dMid = Math.abs(newNodePosition.y - destinationNode.getLocation().y);
						double dMax = Math.abs(newNodePosition.z - destinationNode.getLocation().z);
						// Sort distance vector in ascending order
						double temp;
						if (dMin > dMid) {
							temp = dMid;
							dMid = dMin;
							dMin = temp;
						}
						if (dMin > dMax) {
							temp = dMax;
							dMax = dMin;
							dMin = temp;
						}
						if (dMid > dMax) {
							temp = dMax;
							dMax = dMid;
							dMid = temp;
						}
						// Octile distance from newNode to station node
						double currentRemaining = 0.317837245195782 * dMin + 0.414213562373095 * dMid + dMax + destination.position;
						if (node2.getLocation().equals(destinationNode))
							currentRemaining -= newEdge.getLength() * 2; // Correct the distance estimator for station edge
						remainingDist = Math.min(remainingDist, currentRemaining);
					}
				}

				reachedVia.putIfAbsent(newEdge, Pair.of(validTargets.size() > 1, Couple.create(node1, node2)));
				frontier.add(new FrontierEntry(newDistance, newPenalty, remainingDist, hasDestination, node2, newNode, newEdge));
			}
		}
	}

	private class FrontierEntry implements Comparable<FrontierEntry> {

		double distance;
		int penalty;
		double remaining;
		boolean hasDestination;
		TrackNode node1;
		TrackNode node2;
		TrackEdge edge;

		public FrontierEntry(double distance, int penalty, TrackNode node1, TrackNode node2, TrackEdge edge) {
			this.distance = distance;
			this.penalty = penalty;
			this.remaining = 0;
			this.hasDestination = false;
			this.node1 = node1;
			this.node2 = node2;
			this.edge = edge;
		}
		public FrontierEntry(double distance, int penalty, double remaining, boolean hasDestination, TrackNode node1, TrackNode node2, TrackEdge edge) {
			this.distance = distance;
			this.penalty = penalty;
			this.remaining = remaining;
			this.hasDestination = hasDestination;
			this.node1 = node1;
			this.node2 = node2;
			this.edge = edge;
		}

		@Override
		public int compareTo(FrontierEntry o) {
			return Double.compare(distance + penalty + remaining, o.distance + o.penalty + o.remaining);
		}

	}

	@FunctionalInterface
	public interface StationTest {
		boolean test(double distance, double cost, Map<TrackEdge, Pair<Boolean, Couple<TrackNode>>> reachedVia,
			Pair<Couple<TrackNode>, TrackEdge> current, GlobalStation station);
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag tag = new CompoundTag();
		if (destination == null)
			return tag;

		removeBrokenPathEntries();

		tag.putUUID("Destination", destination.id);
		tag.putDouble("DistanceToDestination", distanceToDestination);
		tag.putDouble("DistanceStartedAt", distanceStartedAt);
		tag.putBoolean("BehindTrain", destinationBehindTrain);
		tag.putBoolean("AnnounceArrival", announceArrival);
		tag.put("Path", NBTHelper.writeCompoundList(currentPath, c -> {
			CompoundTag nbt = new CompoundTag();
			nbt.put("Nodes", c.map(TrackNode::getLocation)
				.serializeEach(loc -> loc.write(dimensions)));
			return nbt;
		}));
		if (waitingForSignal == null)
			return tag;
		tag.putUUID("BlockingSignal", waitingForSignal.getFirst());
		tag.putBoolean("BlockingSignalSide", waitingForSignal.getSecond());
		tag.putDouble("DistanceToSignal", distanceToSignal);
		tag.putInt("TicksWaitingForSignal", ticksWaitingForSignal);
		return tag;
	}

	public void read(CompoundTag tag, TrackGraph graph, DimensionPalette dimensions) {
		destination = graph != null && tag.contains("Destination")
			? graph.getPoint(EdgePointType.STATION, tag.getUUID("Destination"))
			: null;

		if (destination == null)
			return;

		distanceToDestination = tag.getDouble("DistanceToDestination");
		distanceStartedAt = tag.getDouble("DistanceStartedAt");
		destinationBehindTrain = tag.getBoolean("BehindTrain");
		announceArrival = tag.getBoolean("AnnounceArrival");
		currentPath.clear();
		NBTHelper.iterateCompoundList(tag.getList("Path", Tag.TAG_COMPOUND),
			c -> currentPath.add(Couple
				.deserializeEach(c.getList("Nodes", Tag.TAG_COMPOUND), c2 -> TrackNodeLocation.read(c2, dimensions))
				.map(graph::locateNode)));
		
		removeBrokenPathEntries();
		
		waitingForSignal = tag.contains("BlockingSignal")
			? Pair.of(tag.getUUID("BlockingSignal"), tag.getBoolean("BlockingSignalSide"))
			: null;
		if (waitingForSignal == null)
			return;
		distanceToSignal = tag.getDouble("DistanceToSignal");
		ticksWaitingForSignal = tag.getInt("TicksWaitingForSignal");
	}

	private void removeBrokenPathEntries() {
		/*
		 * Trains might load or save with null entries in their path, this method avoids
		 * that anomaly from causing NPEs. The underlying issue has not been found.
		 */
		
		boolean nullEntriesPresent = false;

		for (Iterator<Couple<TrackNode>> iterator = currentPath.iterator(); iterator.hasNext();) {
			Couple<TrackNode> couple = iterator.next();
			if (couple == null || couple.getFirst() == null || couple.getSecond() == null) {
				iterator.remove();
				nullEntriesPresent = true;
			}
		}

		if (nullEntriesPresent)
			Create.LOGGER.error("Found null values in path of train with name: " + train.name.getString() + ", id: "
				+ train.id.toString());
	}

}
