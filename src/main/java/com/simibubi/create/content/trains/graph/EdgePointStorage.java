package com.simibubi.create.content.trains.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class EdgePointStorage {

	private Map<EdgePointType<?>, Map<UUID, TrackEdgePoint>> pointsByType;

	public EdgePointStorage() {
		pointsByType = new HashMap<>();
	}

	public <T extends TrackEdgePoint> void put(EdgePointType<T> type, TrackEdgePoint point) {
		getMap(type).put(point.getId(), point);
	}

	@SuppressWarnings("unchecked")
	public <T extends TrackEdgePoint> T get(EdgePointType<T> type, UUID id) {
		return (T) getMap(type).get(id);
	}

	@SuppressWarnings("unchecked")
	public <T extends TrackEdgePoint> T remove(EdgePointType<T> type, UUID id) {
		return (T) getMap(type).remove(id);
	}

	@SuppressWarnings("unchecked")
	public <T extends TrackEdgePoint> Collection<T> values(EdgePointType<T> type) {
		return getMap(type).values()
			.stream()
			.map(e -> (T) e)
			.toList();
	}

	public Map<UUID, TrackEdgePoint> getMap(EdgePointType<? extends TrackEdgePoint> type) {
		return pointsByType.computeIfAbsent(type, t -> new HashMap<>());
	}

	public void tick(TrackGraph graph, boolean preTrains) {
		pointsByType.values()
			.forEach(map -> map.values()
				.forEach(p -> p.tick(graph, preTrains)));
	}

	public void transferAll(TrackGraph target, EdgePointStorage other) {
		pointsByType.forEach((type, map) -> {
			other.getMap(type)
				.putAll(map);
			map.values()
				.forEach(ep -> Create.RAILWAYS.sync.pointAdded(target, ep));
		});
		pointsByType.clear();
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag nbt = new CompoundTag();
		for (Entry<EdgePointType<?>, Map<UUID, TrackEdgePoint>> entry : pointsByType.entrySet()) {
			EdgePointType<?> type = entry.getKey();
			ListTag list = NBTHelper.writeCompoundList(entry.getValue()
				.values(), edgePoint -> {
					CompoundTag tag = new CompoundTag();
					edgePoint.write(tag, dimensions);
					return tag;
				});
			nbt.put(type.getId()
				.toString(), list);
		}
		return nbt;
	}

	public void read(CompoundTag nbt, DimensionPalette dimensions) {
		for (EdgePointType<?> type : EdgePointType.TYPES.values()) {
			ListTag list = nbt.getList(type.getId()
				.toString(), Tag.TAG_COMPOUND);
			Map<UUID, TrackEdgePoint> map = getMap(type);
			NBTHelper.iterateCompoundList(list, tag -> {
				TrackEdgePoint edgePoint = type.create();
				edgePoint.read(tag, false, dimensions);
				map.put(edgePoint.getId(), edgePoint);
			});
		}
	}

}
