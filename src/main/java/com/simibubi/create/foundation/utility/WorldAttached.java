package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.util.NonNullFunction;

public class WorldAttached<T> {

	static List<Map<LevelAccessor, ?>> allMaps = new ArrayList<>();
	Map<LevelAccessor, T> attached;
	private final NonNullFunction<LevelAccessor, T> factory;

	public WorldAttached(NonNullFunction<LevelAccessor, T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
		allMaps.add(attached);
	}

	public static void invalidateWorld(LevelAccessor world) {
		allMaps.forEach(m -> m.remove(world));
	}

	@Nonnull
	public T get(LevelAccessor world) {
		T t = attached.get(world);
		if (t != null)
			return t;
		T entry = factory.apply(world);
		put(world, entry);
		return entry;
	}

	public void put(LevelAccessor world, T entry) {
		attached.put(world, entry);
	}

}
