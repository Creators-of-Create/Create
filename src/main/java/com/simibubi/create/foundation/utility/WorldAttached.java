package com.simibubi.create.foundation.utility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.world.level.LevelAccessor;

public class WorldAttached<T> {

	// weak references to prevent leaking hashmaps when a WorldAttached is GC'd during runtime
	static List<WeakReference<Map<LevelAccessor, ?>>> allMaps = new ArrayList<>();
	private final Map<LevelAccessor, T> attached;
	private final Function<LevelAccessor, T> factory;

	public WorldAttached(Function<LevelAccessor, T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
		allMaps.add(new WeakReference<>(attached));
	}

	public static void invalidateWorld(LevelAccessor world) {
		var i = allMaps.iterator();
		while (i.hasNext()) {
			Map<LevelAccessor, ?> map = i.next()
					.get();
			if (map == null) {
				// If the map has been GC'd, remove the weak reference
				i.remove();
			} else {
				// Prevent leaks
				map.remove(world);
			}
		}
	}

	@Nonnull
	public T get(LevelAccessor world) {
		T t = attached.get(world);
		if (t != null) return t;
		T entry = factory.apply(world);
		put(world, entry);
		return entry;
	}

	public void put(LevelAccessor world, T entry) {
		attached.put(world, entry);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@Nonnull
	public T replace(LevelAccessor world) {
		attached.remove(world);

		return get(world);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@Nonnull
	public T replace(LevelAccessor world, Consumer<T> finalizer) {
		T remove = attached.remove(world);

		if (remove != null)
			finalizer.accept(remove);

		return get(world);
	}

	/**
	 * Deletes all entries after calling a function on them.
	 *
	 * @param finalizer Do something with all of the world-value pairs
	 */
	public void empty(BiConsumer<LevelAccessor, T> finalizer) {
		attached.forEach(finalizer);
		attached.clear();
	}

	/**
	 * Deletes all entries after calling a function on them.
	 *
	 * @param finalizer Do something with all of the values
	 */
	public void empty(Consumer<T> finalizer) {
		attached.values()
				.forEach(finalizer);
		attached.clear();
	}
}
