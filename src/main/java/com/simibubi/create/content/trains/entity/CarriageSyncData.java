package com.simibubi.create.content.trains.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CarriageSyncData {

	public Vector<Pair<Couple<Integer>, Float>> wheelLocations;
	public Pair<Vec3, Couple<Vec3>> fallbackLocations;
	public float distanceToDestination;
	public boolean leadingCarriage;

	// For Client interpolation
	private Pair<Vec3, Couple<Vec3>> fallbackPointSnapshot;
	private TravellingPoint[] pointsToApproach;
	private float[] pointDistanceSnapshot;
	private float destinationDistanceSnapshot;
	private int ticksSince;

	public CarriageSyncData() {
		wheelLocations = new Vector<>(4);
		fallbackLocations = null;
		pointDistanceSnapshot = new float[4];
		pointsToApproach = new TravellingPoint[4];
		fallbackPointSnapshot = null;
		destinationDistanceSnapshot = 0;
		leadingCarriage = false;
		ticksSince = 0;
		for (int i = 0; i < 4; i++) {
			wheelLocations.add(null);
			pointsToApproach[i] = new TravellingPoint();
		}
	}

	public CarriageSyncData copy() {
		CarriageSyncData data = new CarriageSyncData();
		for (int i = 0; i < 4; i++)
			data.wheelLocations.set(i, wheelLocations.get(i));
		if (fallbackLocations != null)
			data.fallbackLocations = fallbackLocations.copy();
		data.distanceToDestination = distanceToDestination;
		data.leadingCarriage = leadingCarriage;
		return data;
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(leadingCarriage);
		buffer.writeBoolean(fallbackLocations != null);

		if (fallbackLocations != null) {
			Vec3 contraptionAnchor = fallbackLocations.getFirst();
			Couple<Vec3> rotationAnchors = fallbackLocations.getSecond();
			VecHelper.write(contraptionAnchor, buffer);
			VecHelper.write(rotationAnchors.getFirst(), buffer);
			VecHelper.write(rotationAnchors.getSecond(), buffer);
			return;
		}

		for (Pair<Couple<Integer>, Float> pair : wheelLocations) {
			buffer.writeBoolean(pair == null);
			if (pair == null)
				break;
			pair.getFirst()
				.forEach(buffer::writeInt);
			buffer.writeFloat(pair.getSecond());
		}
		buffer.writeFloat(distanceToDestination);
	}

	public void read(FriendlyByteBuf buffer) {
		leadingCarriage = buffer.readBoolean();
		boolean fallback = buffer.readBoolean();
		ticksSince = 0;

		if (fallback) {
			fallbackLocations =
				Pair.of(VecHelper.read(buffer), Couple.create(VecHelper.read(buffer), VecHelper.read(buffer)));
			return;
		}

		fallbackLocations = null;
		for (int i = 0; i < 4; i++) {
			if (buffer.readBoolean())
				break;
			wheelLocations.set(i, Pair.of(Couple.create(buffer::readInt), buffer.readFloat()));
		}
		distanceToDestination = buffer.readFloat();
	}

	public void update(CarriageContraptionEntity entity, Carriage carriage) {
		DimensionalCarriageEntity dce = carriage.getDimensional(entity.level);

		TrackGraph graph = carriage.train.graph;
		if (graph == null) {
			updateFallbackLocations(dce);
			return;
		}

		fallbackLocations = null;
		leadingCarriage = entity.carriageIndex == (carriage.train.speed >= 0 ? 0 : carriage.train.carriages.size() - 1);

		for (boolean first : Iterate.trueAndFalse) {
			if (!first && !carriage.isOnTwoBogeys())
				break;
			
			CarriageBogey bogey = carriage.bogeys.get(first);
			for (boolean firstPoint : Iterate.trueAndFalse) {
				TravellingPoint point = bogey.points.get(firstPoint);
				int index = (first ? 0 : 2) + (firstPoint ? 0 : 1);
				Couple<TrackNode> nodes = Couple.create(point.node1, point.node2);

				if (nodes.either(Objects::isNull)) {
					updateFallbackLocations(dce);
					return;
				}

				wheelLocations.set(index, Pair.of(nodes.map(TrackNode::getNetId), (float) point.position));
			}
		}

		distanceToDestination = (float) carriage.train.navigation.distanceToDestination;
		setDirty(true);
	}

	private void updateFallbackLocations(DimensionalCarriageEntity dce) {
		fallbackLocations = Pair.of(dce.positionAnchor, dce.rotationAnchors);
		dce.pointsInitialised = true;
		setDirty(true);
	}

	public void apply(CarriageContraptionEntity entity, Carriage carriage) {
		DimensionalCarriageEntity dce = carriage.getDimensional(entity.level);

		fallbackPointSnapshot = null;
		if (fallbackLocations != null) {
			fallbackPointSnapshot = Pair.of(dce.positionAnchor, dce.rotationAnchors);
			dce.pointsInitialised = true;
			return;
		}

		TrackGraph graph = carriage.train.graph;
		if (graph == null)
			return;

		for (int i = 0; i < wheelLocations.size(); i++) {
			Pair<Couple<Integer>, Float> pair = wheelLocations.get(i);
			if (pair == null)
				break;

			CarriageBogey bogey = carriage.bogeys.get(i / 2 == 0);
			TravellingPoint bogeyPoint = bogey.points.get(i % 2 == 0);
			TravellingPoint point = dce.pointsInitialised ? pointsToApproach[i] : bogeyPoint;

			Couple<TrackNode> nodes = pair.getFirst()
				.map(graph::getNode);
			if (nodes.either(Objects::isNull))
				continue;
			TrackEdge edge = graph.getConnectionsFrom(nodes.getFirst())
				.get(nodes.getSecond());
			if (edge == null)
				continue;

			point.node1 = nodes.getFirst();
			point.node2 = nodes.getSecond();
			point.edge = edge;
			point.position = pair.getSecond();

			if (dce.pointsInitialised) {
				float foundDistance = -1;
				boolean direction = false;
				for (boolean forward : Iterate.trueAndFalse) {
					float distanceTo = getDistanceTo(graph, bogeyPoint, point, foundDistance, forward);
					if (distanceTo > 0 && (foundDistance == -1 || distanceTo < foundDistance)) {
						foundDistance = distanceTo;
						direction = forward;
					}
				}

				if (foundDistance != -1) {
					pointDistanceSnapshot[i] = (direction ? 1 : -1) * foundDistance;
				} else {
					// could not pathfind to server location
					bogeyPoint.node1 = point.node1;
					bogeyPoint.node2 = point.node2;
					bogeyPoint.edge = point.edge;
					bogeyPoint.position = point.position;
					pointDistanceSnapshot[i] = 0;
				}
			}
		}

		if (!dce.pointsInitialised) {
			carriage.train.navigation.distanceToDestination = distanceToDestination;
			dce.pointsInitialised = true;
			return;
		}

		if (!leadingCarriage)
			return;

		destinationDistanceSnapshot = (float) (distanceToDestination - carriage.train.navigation.distanceToDestination);
	}

	public void approach(CarriageContraptionEntity entity, Carriage carriage, float partialIn) {
		DimensionalCarriageEntity dce = carriage.getDimensional(entity.level);
		
		int updateInterval = entity.getType()
			.updateInterval();
		if (ticksSince >= updateInterval * 2)
			partialIn /= ticksSince - updateInterval * 2 + 1;
		partialIn *= ServerSpeedProvider.get();
		final float partial = partialIn;
		
		ticksSince++;

		if (fallbackLocations != null && fallbackPointSnapshot != null) {
			dce.positionAnchor = approachVector(partial, dce.positionAnchor, fallbackLocations.getFirst(),
				fallbackPointSnapshot.getFirst());
			dce.rotationAnchors.replaceWithContext((current, first) -> approachVector(partial, current,
				fallbackLocations.getSecond()
					.get(first),
				fallbackPointSnapshot.getSecond()
					.get(first)));
			return;
		}

		TrackGraph graph = carriage.train.graph;
		if (graph == null)
			return;

		carriage.train.navigation.distanceToDestination += partial * destinationDistanceSnapshot;

		for (boolean first : Iterate.trueAndFalse) {
			if (!first && !carriage.isOnTwoBogeys())
				break;
			CarriageBogey bogey = carriage.bogeys.get(first);
			for (boolean firstPoint : Iterate.trueAndFalse) {
				int index = (first ? 0 : 2) + (firstPoint ? 0 : 1);
				float f = pointDistanceSnapshot[index];
				if (Mth.equal(f, 0))
					continue;

				TravellingPoint point = bogey.points.get(firstPoint);
				MutableBoolean success = new MutableBoolean(true);
				TravellingPoint toApproach = pointsToApproach[index];

				ITrackSelector trackSelector =
					point.follow(toApproach, b -> success.setValue(success.booleanValue() && b));
				point.travel(graph, partial * f, trackSelector);

				// could not pathfind to server location
				if (!success.booleanValue()) {
					point.node1 = toApproach.node1;
					point.node2 = toApproach.node2;
					point.edge = toApproach.edge;
					point.position = toApproach.position;
					pointDistanceSnapshot[index] = 0;
				}
			}
		}
	}

	private Vec3 approachVector(float partial, Vec3 current, Vec3 target, Vec3 snapshot) {
		if (current == null || snapshot == null)
			return target;
		return current.add(target.subtract(snapshot)
			.scale(partial));
	}

	public float getDistanceTo(TrackGraph graph, TravellingPoint current, TravellingPoint target, float maxDistance,
		boolean forward) {
		if (maxDistance == -1)
			maxDistance = 32;

		Set<TrackEdge> visited = new HashSet<>();
		Map<TrackEdge, Pair<Boolean, TrackEdge>> reachedVia = new IdentityHashMap<>();
		PriorityQueue<Pair<Double, Pair<Couple<TrackNode>, TrackEdge>>> frontier =
			new PriorityQueue<>((p1, p2) -> Double.compare(p1.getFirst(), p2.getFirst()));

		TrackNode initialNode1 = forward ? current.node1 : current.node2;
		TrackNode initialNode2 = forward ? current.node2 : current.node1;
		TrackEdge initialEdge = graph.getConnectionsFrom(initialNode1)
			.get(initialNode2);

		if (initialEdge == null)
			return -1; // graph changed

		TrackNode targetNode1 = forward ? target.node1 : target.node2;
		TrackNode targetNode2 = forward ? target.node2 : target.node1;
		TrackEdge targetEdge = graph.getConnectionsFrom(targetNode1)
			.get(targetNode2);

		double distanceToNode2 = forward ? initialEdge.getLength() - current.position : current.position;

		frontier.add(Pair.of(distanceToNode2, Pair.of(Couple.create(initialNode1, initialNode2), initialEdge)));

		while (!frontier.isEmpty()) {
			Pair<Double, Pair<Couple<TrackNode>, TrackEdge>> poll = frontier.poll();
			double distance = poll.getFirst();

			Pair<Couple<TrackNode>, TrackEdge> currentEntry = poll.getSecond();
			TrackNode node2 = currentEntry.getFirst()
				.getSecond();
			TrackEdge edge = currentEntry.getSecond();

			if (edge == targetEdge)
				return (float) (distance - (forward ? edge.getLength() - target.position : target.position));

			if (distance > maxDistance)
				continue;

			List<Entry<TrackNode, TrackEdge>> validTargets = new ArrayList<>();
			Map<TrackNode, TrackEdge> connectionsFrom = graph.getConnectionsFrom(node2);
			for (Entry<TrackNode, TrackEdge> entry : connectionsFrom.entrySet()) {
				TrackEdge newEdge = entry.getValue();
				Vec3 currentDirection = edge.getDirection(false);
				Vec3 newDirection = newEdge.getDirection(true);
				if (currentDirection.dot(newDirection) < 7 / 8f)
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
				frontier.add(Pair.of(newEdge.getLength() + distance, Pair.of(Couple.create(node2, newNode), newEdge)));
			}
		}

		return -1;
	}

	//

	private boolean isDirty;

	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}

	public boolean isDirty() {
		return isDirty;
	}

}
