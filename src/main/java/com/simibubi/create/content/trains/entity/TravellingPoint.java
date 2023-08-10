package com.simibubi.create.content.trains.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TravellingPoint {

	public TrackNode node1, node2;
	public TrackEdge edge;
	public double position;
	public boolean blocked;
	public boolean upsideDown;

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

	public static interface IEdgePointListener extends BiPredicate<Double, Pair<TrackEdgePoint, Couple<TrackNode>>> {
	};

	public static interface ITurnListener extends BiConsumer<Double, TrackEdge> {
	};

	public static interface IPortalListener extends Predicate<Couple<TrackNodeLocation>> {
	};

	public TravellingPoint() {}

	public TravellingPoint(TrackNode node1, TrackNode node2, TrackEdge edge, double position, boolean upsideDown) {
		this.node1 = node1;
		this.node2 = node2;
		this.edge = edge;
		this.position = position;
		this.upsideDown = upsideDown;
	}

	public IEdgePointListener ignoreEdgePoints() {
		return (d, c) -> false;
	}

	public ITurnListener ignoreTurns() {
		return (d, c) -> {
		};
	}

	public IPortalListener ignorePortals() {
		return $ -> false;
	}

	public ITrackSelector random() {
		return (graph, pair) -> pair.getSecond()
			.get(Create.RANDOM.nextInt(pair.getSecond()
				.size()));
	}

	public ITrackSelector follow(TravellingPoint other) {
		return follow(other, null);
	}

	public ITrackSelector follow(TravellingPoint other, @Nullable Consumer<Boolean> success) {
		return (graph, pair) -> {
			List<Entry<TrackNode, TrackEdge>> validTargets = pair.getSecond();
			boolean forward = pair.getFirst();
			TrackNode target = forward ? other.node1 : other.node2;
			TrackNode secondary = forward ? other.node2 : other.node1;

			for (Entry<TrackNode, TrackEdge> entry : validTargets)
				if (entry.getKey() == target || entry.getKey() == secondary) {
					if (success != null)
						success.accept(true);
					return entry;
				}

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
						if (!currentEntry.getValue()
							.canTravelTo(nextEdge))
							continue;

						TrackNode nextNode = nextEntry.getKey();
						if (nextNode == target) {
							if (success != null)
								success.accept(true);
							return entry;
						}

						frontier.add(nextEntry);
					}
				}
			}

			if (success != null)
				success.accept(false);
			return validTargets.get(0);
		};
	}

	public ITrackSelector steer(SteerDirection direction, Vec3 upNormal) {
		return (graph, pair) -> {
			List<Entry<TrackNode, TrackEdge>> validTargets = pair.getSecond();
			double closest = Double.MAX_VALUE;
			Entry<TrackNode, TrackEdge> best = null;

			for (Entry<TrackNode, TrackEdge> entry : validTargets) {
				Vec3 trajectory = edge.getDirection(false);
				Vec3 entryTrajectory = entry.getValue()
					.getDirection(true);
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

	public double travel(TrackGraph graph, double distance, ITrackSelector trackSelector) {
		return travel(graph, distance, trackSelector, ignoreEdgePoints());
	}

	public double travel(TrackGraph graph, double distance, ITrackSelector trackSelector,
		IEdgePointListener signalListener) {
		return travel(graph, distance, trackSelector, signalListener, ignoreTurns());
	}

	public double travel(TrackGraph graph, double distance, ITrackSelector trackSelector,
		IEdgePointListener signalListener, ITurnListener turnListener) {
		return travel(graph, distance, trackSelector, signalListener, turnListener, ignorePortals());
	}

	public double travel(TrackGraph graph, double distance, ITrackSelector trackSelector,
		IEdgePointListener signalListener, ITurnListener turnListener, IPortalListener portalListener) {
		blocked = false;
		if (edge == null)
			return 0;
		double edgeLength = edge.getLength();
		if (Mth.equal(distance, 0))
			return 0;

		double prevPos = position;
		double traveled = distance;
		double currentT = edgeLength == 0 ? 0 : position / edgeLength;
		double incrementT = edge.incrementT(currentT, distance);
		position = incrementT * edgeLength;

		// FIXME: using incrementT like this becomes inaccurate at medium-long distances
		// travelling points would travel only 50m instead of 100m due to the low
		// incrementT at their starting position (e.g. bezier turn)
		// In an ideal scenario the amount added to position would iterate the traversed
		// edges for context first

		// A workaround was added in TrackEdge::incrementT

		List<Entry<TrackNode, TrackEdge>> validTargets = new ArrayList<>();

		boolean forward = distance > 0;
		double collectedDistance = forward ? -prevPos : -edgeLength + prevPos;

		Double blockedLocation =
			edgeTraversedFrom(graph, forward, signalListener, turnListener, prevPos, collectedDistance);
		if (blockedLocation != null) {
			position = blockedLocation.doubleValue();
			traveled = position - prevPos;
			return traveled;
		}

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
					if (!edge.canTravelTo(newEdge))
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

				if (entry.getValue()
					.getLength() == 0 && portalListener.test(
						Couple.create(node2.getLocation(), entry.getKey()
							.getLocation()))) {
					traveled -= position - edgeLength;
					position = edgeLength;
					blocked = true;
					break;
				}

				node1 = node2;
				node2 = entry.getKey();
				edge = entry.getValue();
				position -= edgeLength;

				collectedDistance += edgeLength;
				if (edge.isTurn())
					turnListener.accept(collectedDistance, edge);

				blockedLocation = edgeTraversedFrom(graph, forward, signalListener, turnListener, 0, collectedDistance);

				if (blockedLocation != null) {
					traveled -= position;
					position = blockedLocation.doubleValue();
					traveled += position;
					break;
				}

				prevPos = 0;
				edgeLength = edge.getLength();
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
					if (!graph.getConnectionsFrom(newNode)
						.get(node1)
						.canTravelTo(edge))
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

				if (entry.getValue()
					.getLength() == 0 && portalListener.test(
						Couple.create(entry.getKey()
							.getLocation(), node1.getLocation()))) {
					traveled -= position;
					position = 0;
					blocked = true;
					break;
				}

				node2 = node1;
				node1 = entry.getKey();
				edge = graph.getConnectionsFrom(node1)
					.get(node2);
				collectedDistance += edgeLength;
				edgeLength = edge.getLength();
				position += edgeLength;

				blockedLocation =
					edgeTraversedFrom(graph, forward, signalListener, turnListener, edgeLength, collectedDistance);

				if (blockedLocation != null) {
					traveled -= position;
					position = blockedLocation.doubleValue();
					traveled += position;
					break;
				}
			}

		}

		return traveled;
	}

	protected Double edgeTraversedFrom(TrackGraph graph, boolean forward, IEdgePointListener edgePointListener,
		ITurnListener turnListener, double prevPos, double totalDistance) {
		if (edge.isTurn())
			turnListener.accept(Math.max(0, totalDistance), edge);

		double from = forward ? prevPos : position;
		double to = forward ? position : prevPos;

		EdgeData edgeData = edge.getEdgeData();
		List<TrackEdgePoint> edgePoints = edgeData.getPoints();

		double length = edge.getLength();
		for (int i = 0; i < edgePoints.size(); i++) {
			int index = forward ? i : edgePoints.size() - i - 1;
			TrackEdgePoint nextBoundary = edgePoints.get(index);
			double locationOn = nextBoundary.getLocationOn(edge);
			double distance = forward ? locationOn : length - locationOn;
			if (forward ? (locationOn < from || locationOn >= to) : (locationOn <= from || locationOn > to))
				continue;
			Couple<TrackNode> nodes = Couple.create(node1, node2);
			if (edgePointListener.test(totalDistance + distance, Pair.of(nextBoundary, forward ? nodes : nodes.swap())))
				return locationOn;
		}

		return null;
	}

	public void reverse(TrackGraph graph) {
		TrackNode n = node1;
		node1 = node2;
		node2 = n;
		position = edge.getLength() - position;
		edge = graph.getConnectionsFrom(node1)
			.get(node2);
	}

	public Vec3 getPosition(@Nullable TrackGraph trackGraph) {
		return getPosition(trackGraph, false);
	}

	public Vec3 getPosition(@Nullable TrackGraph trackGraph, boolean flipUpsideDown) {
		return getPositionWithOffset(trackGraph, 0, flipUpsideDown);
	}

	public Vec3 getPositionWithOffset(@Nullable TrackGraph trackGraph, double offset, boolean flipUpsideDown) {
		double t = (position + offset) / edge.getLength();
		return edge.getPosition(trackGraph, t)
			.add(edge.getNormal(trackGraph, t)
				.scale(upsideDown ^ flipUpsideDown ? -1 : 1));
	}

	public void migrateTo(List<TrackGraphLocation> locations) {
		TrackGraphLocation location = locations.remove(0);
		TrackGraph graph = location.graph;
		node1 = graph.locateNode(location.edge.getFirst());
		node2 = graph.locateNode(location.edge.getSecond());
		position = location.position;
		edge = graph.getConnectionsFrom(node1)
			.get(node2);
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag tag = new CompoundTag();
		Couple<TrackNode> nodes = Couple.create(node1, node2);
		if (nodes.either(Objects::isNull))
			return tag;
		tag.put("Nodes", nodes.map(TrackNode::getLocation)
			.serializeEach(loc -> loc.write(dimensions)));
		tag.putDouble("Position", position);
		tag.putBoolean("UpsideDown", upsideDown);
		return tag;
	}

	public static TravellingPoint read(CompoundTag tag, TrackGraph graph, DimensionPalette dimensions) {
		if (graph == null)
			return new TravellingPoint(null, null, null, 0, false);

		Couple<TrackNode> locs = tag.contains("Nodes")
			? Couple.deserializeEach(tag.getList("Nodes", Tag.TAG_COMPOUND), c -> TrackNodeLocation.read(c, dimensions))
				.map(graph::locateNode)
			: Couple.create(null, null);

		if (locs.either(Objects::isNull))
			return new TravellingPoint(null, null, null, 0, false);

		double position = tag.getDouble("Position");
		return new TravellingPoint(locs.getFirst(), locs.getSecond(), graph.getConnectionsFrom(locs.getFirst())
			.get(locs.getSecond()), position, tag.getBoolean("UpsideDown"));
	}

}
