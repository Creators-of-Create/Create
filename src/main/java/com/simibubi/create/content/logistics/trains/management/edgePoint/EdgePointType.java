package com.simibubi.create.content.logistics.trains.management.edgePoint;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.signal.SignalBoundary;
import com.simibubi.create.content.logistics.trains.management.signal.TrackEdgePoint;

import net.minecraft.resources.ResourceLocation;

public class EdgePointType<T extends TrackEdgePoint> {

	public static final Map<ResourceLocation, EdgePointType<?>> TYPES = new HashMap<>();
	private ResourceLocation id;
	private Supplier<T> factory;

	public static final EdgePointType<SignalBoundary> SIGNAL =
		register(Create.asResource("signal"), SignalBoundary::new);
	public static final EdgePointType<GlobalStation> STATION =
		register(Create.asResource("station"), GlobalStation::new);

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

}
