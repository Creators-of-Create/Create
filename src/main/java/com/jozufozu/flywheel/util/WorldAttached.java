package com.jozufozu.flywheel.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.NonNullSupplier;

public class WorldAttached<T> {

	Map<IWorld, T> attached;
	private final NonNullSupplier<T> factory;

	public WorldAttached(NonNullSupplier<T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
	}

	@Nonnull
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
		attached.values()
				.forEach(consumer);
	}

}
