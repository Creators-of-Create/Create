package com.simibubi.create.impl.registry;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class MappedRegistryWithFreezeCallback<T> extends MappedRegistry<T> {
	private final Runnable freezeCallback;

	public MappedRegistryWithFreezeCallback(ResourceKey<? extends Registry<T>> key, Lifecycle registryLifecycle, Runnable freezeCallback) {
		super(key, registryLifecycle);
		this.freezeCallback = freezeCallback;
	}

	@Override
	public @NotNull Registry<T> freeze() {
		freezeCallback.run();
		return super.freeze();
	}
}
