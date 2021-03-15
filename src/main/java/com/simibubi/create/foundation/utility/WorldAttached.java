package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.world.IWorld;

public class WorldAttached<T> {

	static List<Map<IWorld, ?>> allMaps = new ArrayList<>();
	Map<IWorld, T> attached;
	private Supplier<T> factory;

	public WorldAttached(Supplier<T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
		allMaps.add(attached);
	}
	
	public static void invalidateWorld(IWorld world) {
		allMaps.forEach(m -> m.remove(world));
	}
	
	@Nullable
	public T get(IWorld world) {
		T t = attached.get(world);
		if (t != null)
			return t;
		T entry = factory.get();
		put(world, entry);
		return entry;
	}
	
	public void put(IWorld world, T entry) {
		attached.put(world, entry);
	}

	public void forEach(Consumer<T> consumer) {
		attached.values().forEach(consumer);
	}
	
}
