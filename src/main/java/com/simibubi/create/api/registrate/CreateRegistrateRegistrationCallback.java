package com.simibubi.create.api.registrate;

import java.util.function.Consumer;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.impl.registrate.CreateRegistrateRegistrationCallbackImpl;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Register a callback for when an entry is added to any {@link CreateRegistrate} instance
 */
public class CreateRegistrateRegistrationCallback {
	public static <T> void register(ResourceKey<? extends Registry<T>> registry, ResourceLocation id, Consumer<T> callback) {
		CreateRegistrateRegistrationCallbackImpl.register(registry, id, callback);
	}
}
