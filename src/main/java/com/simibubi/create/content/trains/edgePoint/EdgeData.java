package com.simibubi.create.content.trains.edgePoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EdgeData {

	public static final UUID passiveGroup = UUID.fromString("00000000-0000-0000-0000-000000000000");

	private UUID singleSignalGroup;
	private List<TrackEdgePoint> points;
	private List<TrackEdgeIntersection> intersections;
	private TrackEdge edge;

	public EdgeData(TrackEdge edge) {
		this.edge = edge;
		points = new ArrayList<>();
		intersections = new ArrayList<>();
		singleSignalGroup = passiveGroup;
	}

	public boolean hasSignalBoundaries() {
		return singleSignalGroup == null;
	}

	public UUID getSingleSignalGroup() {
		return singleSignalGroup;
	}

	public void setSingleSignalGroup(@Nullable TrackGraph graph, UUID singleSignalGroup) {
		if (graph != null && !Objects.equal(singleSignalGroup, this.singleSignalGroup))
			refreshIntersectingSignalGroups(graph);
		this.singleSignalGroup = singleSignalGroup;
	}

	public void refreshIntersectingSignalGroups(TrackGraph graph) {
		Map<UUID, SignalEdgeGroup> groups = Create.RAILWAYS.signalEdgeGroups;
		for (TrackEdgeIntersection intersection : intersections) {
			if (intersection.groupId == null)
				continue;
			SignalEdgeGroup group = groups.get(intersection.groupId);
			if (group != null)
				group.removeIntersection(intersection.id);
		}
		if (hasIntersections())
			graph.deferIntersectionUpdate(edge);
	}

	public boolean hasPoints() {
		return !points.isEmpty();
	}

	public boolean hasIntersections() {
		return !intersections.isEmpty();
	}

	public List<TrackEdgeIntersection> getIntersections() {
		return intersections;
	}

	public void addIntersection(TrackGraph graph, UUID id, double position, TrackNode target1, TrackNode target2,
		double targetPosition) {
		TrackNodeLocation loc1 = target1.getLocation();
		TrackNodeLocation loc2 = target2.getLocation();

		for (TrackEdgeIntersection existing : intersections)
			if (existing.isNear(position) && existing.targets(loc1, loc2))
				return;

		TrackEdgeIntersection intersection = new TrackEdgeIntersection();
		intersection.id = id;
		intersection.location = position;
		intersection.target = Couple.create(loc1, loc2);
		intersection.targetLocation = targetPosition;
		intersections.add(intersection);
		graph.deferIntersectionUpdate(edge);
	}

	public void removeIntersection(TrackGraph graph, UUID id) {
		refreshIntersectingSignalGroups(graph);
		for (Iterator<TrackEdgeIntersection> iterator = intersections.iterator(); iterator.hasNext();) {
			TrackEdgeIntersection existing = iterator.next();
			if (existing.id.equals(id))
				iterator.remove();
		}
	}

	public UUID getGroupAtPosition(TrackGraph graph, double position) {
		if (!hasSignalBoundaries())
			return getEffectiveEdgeGroupId(graph);
		SignalBoundary firstSignal = next(EdgePointType.SIGNAL, 0);
		UUID currentGroup = firstSignal.getGroup(edge.node1);

		for (TrackEdgePoint trackEdgePoint : getPoints()) {
			if (!(trackEdgePoint instanceof SignalBoundary sb))
				continue;
			if (sb.getLocationOn(edge) >= position)
				return currentGroup;
			currentGroup = sb.getGroup(edge.node2);
		}

		return currentGroup;
	}

	public List<TrackEdgePoint> getPoints() {
		return points;
	}

	public UUID getEffectiveEdgeGroupId(TrackGraph graph) {
		return singleSignalGroup == null ? null : singleSignalGroup.equals(passiveGroup) ? graph.id : singleSignalGroup;
	}

	public void removePoint(TrackGraph graph, TrackEdgePoint point) {
		points.remove(point);
		if (point.getType() == EdgePointType.SIGNAL) {
			boolean noSignalsRemaining = next(point.getType(), 0) == null;
			setSingleSignalGroup(graph, noSignalsRemaining ? passiveGroup : null);
		}
	}

	public <T extends TrackEdgePoint> void addPoint(TrackGraph graph, TrackEdgePoint point) {
		if (point.getType() == EdgePointType.SIGNAL)
			setSingleSignalGroup(graph, null);
		double locationOn = point.getLocationOn(edge);
		int i = 0;
		for (; i < points.size(); i++)
			if (points.get(i)
				.getLocationOn(edge) > locationOn)
				break;
		points.add(i, point);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends TrackEdgePoint> T next(EdgePointType<T> type, double minPosition) {
		for (TrackEdgePoint point : points)
			if (point.getType() == type && point.getLocationOn(edge) > minPosition)
				return (T) point;
		return null;
	}

	@Nullable
	public TrackEdgePoint next(double minPosition) {
		for (TrackEdgePoint point : points)
			if (point.getLocationOn(edge) > minPosition)
				return point;
		return null;
	}

	@Nullable
	public <T extends TrackEdgePoint> T get(EdgePointType<T> type, double exactPosition) {
		T next = next(type, exactPosition - .5f);
		if (next != null && Mth.equal(next.getLocationOn(edge), exactPosition))
			return next;
		return null;
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag nbt = new CompoundTag();
		if (singleSignalGroup == passiveGroup)
			NBTHelper.putMarker(nbt, "PassiveGroup");
		else if (singleSignalGroup != null)
			nbt.putUUID("SignalGroup", singleSignalGroup);

		if (hasPoints())
			nbt.put("Points", NBTHelper.writeCompoundList(points, point -> {
				CompoundTag tag = new CompoundTag();
				tag.putUUID("Id", point.id);
				tag.putString("Type", point.getType()
					.getId()
					.toString());
				return tag;
			}));
		if (hasIntersections())
			nbt.put("Intersections", NBTHelper.writeCompoundList(intersections, tei -> tei.write(dimensions)));
		return nbt;
	}

	public static EdgeData read(CompoundTag nbt, TrackEdge edge, TrackGraph graph, DimensionPalette dimensions) {
		EdgeData data = new EdgeData(edge);
		if (nbt.contains("SignalGroup"))
			data.singleSignalGroup = nbt.getUUID("SignalGroup");
		else if (!nbt.contains("PassiveGroup"))
			data.singleSignalGroup = null;

		if (nbt.contains("Points"))
			NBTHelper.iterateCompoundList(nbt.getList("Points", Tag.TAG_COMPOUND), tag -> {
				ResourceLocation location = new ResourceLocation(tag.getString("Type"));
				EdgePointType<?> type = EdgePointType.TYPES.get(location);
				if (type == null || !tag.contains("Id"))
					return;
				TrackEdgePoint point = graph.getPoint(type, tag.getUUID("Id"));
				if (point != null)
					data.points.add(point);
			});
		if (nbt.contains("Intersections"))
			data.intersections = NBTHelper.readCompoundList(nbt.getList("Intersections", Tag.TAG_COMPOUND),
				c -> TrackEdgeIntersection.read(c, dimensions));
		return data;
	}

}
