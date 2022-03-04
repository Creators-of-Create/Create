package com.simibubi.create.content.logistics.trains.management.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EdgeData {

	public static final UUID passiveGroup = UUID.fromString("00000000-0000-0000-0000-000000000000");

	public UUID singleSignalGroup;
	private List<TrackEdgePoint> points;

	public EdgeData() {
		points = new ArrayList<>();
		singleSignalGroup = passiveGroup;
	}

	public boolean hasSignalBoundaries() {
		return singleSignalGroup == null;
	}

	public boolean hasPoints() {
		return !points.isEmpty();
	}

	public List<TrackEdgePoint> getPoints() {
		return points;
	}

	public void removePoint(TrackNode node1, TrackNode node2, TrackEdge edge, TrackEdgePoint point) {
		points.remove(point);
		if (point.getType() == EdgePointType.SIGNAL)
			singleSignalGroup = next(point.getType(), node1, node2, edge, 0) == null ? passiveGroup : null;
	}

	public <T extends TrackEdgePoint> void addPoint(TrackNode node1, TrackNode node2, TrackEdge edge,
		TrackEdgePoint point) {
		if (point.getType() == EdgePointType.SIGNAL)
			singleSignalGroup = null;
		double locationOn = point.getLocationOn(node1, node2, edge);
		int i = 0;
		for (; i < points.size(); i++)
			if (points.get(i)
				.getLocationOn(node1, node2, edge) > locationOn)
				break;
		points.add(i, point);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends TrackEdgePoint> T next(EdgePointType<T> type, TrackNode node1, TrackNode node2, TrackEdge edge,
		double minPosition) {
		for (TrackEdgePoint point : points)
			if (point.getType() == type && point.getLocationOn(node1, node2, edge) > minPosition)
				return (T) point;
		return null;
	}

	@Nullable
	public <T extends TrackEdgePoint> T get(EdgePointType<T> type, TrackNode node1, TrackNode node2, TrackEdge edge,
		double exactPosition) {
		T next = next(type, node1, node2, edge, exactPosition - .5f);
		if (next != null && Mth.equal(next.getLocationOn(node1, node2, edge), exactPosition))
			return next;
		return null;
	}

	public CompoundTag write() {
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
		return nbt;
	}

	public static EdgeData read(CompoundTag nbt, TrackGraph graph) {
		EdgeData data = new EdgeData();
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
		return data;
	}

}
