package com.simibubi.create.api.registrate;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.impl.registrate.CreateRegistrateRegistrationCallbackImpl;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Register a callback for when an entry is added to any {@link CreateRegistrate} instance
 */
public class CreateRegistrateRegistrationCallback {
	public static <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, NonNullConsumer<? super T> callback) {
		CreateRegistrateRegistrationCallbackImpl.<R, T>register(registry, id, callback);
	}

	/**
	 * Provide a {@link CreateRegistrate} instance to be used by the API.
	 * Instances created by {@link CreateRegistrate#create(String)} will automatically be registered.
	 * It is illegal to call this method more than once for the same mod ID.
	 */
	public static void provideRegistrate(CreateRegistrate registrate) {
		CreateRegistrateRegistrationCallbackImpl.provideRegistrate(registrate);
	}

	private CreateRegistrateRegistrationCallback() {
		throw new AssertionError("This class should not be instantiated");
	}
}
