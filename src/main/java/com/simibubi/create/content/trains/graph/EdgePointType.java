package com.simibubi.create.content.trains.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entry.GlobalEntry;
import com.simibubi.create.content.trains.observer.TrackObserver;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.platform.GlobalPlatform;

import com.simibubi.create.content.trains.station.GlobalStation;

import com.simibubi.create.content.trains.waypoint.GlobalWaypoint;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class EdgePointType<T extends TrackEdgePoint> {

	public static final Map<ResourceLocation, EdgePointType<?>> TYPES = new HashMap<>();
	private ResourceLocation id;
	private Supplier<T> factory;

	public static final EdgePointType<SignalBoundary> SIGNAL =
		register(Create.asResource("signal"), SignalBoundary::new);
	public static final EdgePointType<GlobalStation> STATION =
		register(Create.asResource("station"), GlobalStation::new);
	public static final EdgePointType<GlobalPlatform> PLATFORM =
		register(Create.asResource("platform"), GlobalPlatform::new);
	public static final EdgePointType<GlobalEntry> ENTRY =
		register(Create.asResource("entry"), GlobalEntry::new);
	public static final EdgePointType<GlobalWaypoint> WAYPOINT =
		register(Create.asResource("waypoint"), GlobalWaypoint::new);

	public static final EdgePointType<TrackObserver> OBSERVER =
		register(Create.asResource("observer"), TrackObserver::new);

	public static <T extends TrackEdgePoint> EdgePointType<T> register(ResourceLocation id, Supplier<T> factory) {
		EdgePointType<T> type = new EdgePointType<>(id, factory);
		TYPES.put(id, type);
		return type;
	}

	public EdgePointType(ResourceLocation id, Supplier<T> factory) {
		this.id = id;
		this.factory = factory;
	}

	public T create() {
		T t = factory.get();
		t.setType(this);
		return t;
	}

	public ResourceLocation getId() {
		return id;
	}

	public static TrackEdgePoint read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		ResourceLocation type = buffer.readResourceLocation();
		EdgePointType<?> edgePointType = TYPES.get(type);
		TrackEdgePoint point = edgePointType.create();
		point.read(buffer, dimensions);
		return point;
	}

}
