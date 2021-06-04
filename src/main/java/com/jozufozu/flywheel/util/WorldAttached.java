package com.jozufozu.flywheel.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.world.IWorld;

public class WorldAttached<T> {

	Map<IWorld, T> attached;
	private final WorldAttacher<T> factory;

	public WorldAttached(WorldAttacher<T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
	}

	@Nonnull
	public T get(IWorld world) {
		T t = attached.get(world);
		if (t != null)
			return t;
		T entry = factory.attach(world);
		put(world, entry);
		return entry;
	}

	public void put(IWorld world, T entry) {
		attached.put(world, entry);
	}

	public void forEach(Consumer<T> consumer) {
		attached.values()
				.forEach(consumer);
	}

	@FunctionalInterface
	public interface WorldAttacher<T> extends Function<IWorld, T> {
		@Nonnull
		T attach(IWorld world);

		@Override
		default T apply(IWorld world) {
			return attach(world);
		}
	}

}
