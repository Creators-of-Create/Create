package com.simibubi.create.impl.registrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class CreateRegistrateRegistrationCallbackImpl {
	// Intentionally not a synchronized map, since all safe accesses have to be synchronized anyway.
	private static final Map<String, Either<List<CallbackImpl<?, ?>>, CreateRegistrate>> CALLBACKS = new HashMap<>();

	public static void provideRegistrate(CreateRegistrate registrate) {
		synchronized (CALLBACKS) {
			String modid = registrate.getModid();

			var either = CALLBACKS.remove(modid);
			if (either != null) {
				var optionalCallbacks = either.left();
				if (optionalCallbacks.isEmpty()) { // in other words, either.right().isPresent()
					throw new IllegalArgumentException("Tried to register a duplicate CreateRegistrate instance for mod ID: " + modid);
				}

				for (CallbackImpl<?, ?> callback : optionalCallbacks.get()) {
					callback.addToRegistrate(registrate);
				}
			}

			CALLBACKS.put(modid, Either.right(registrate));
		}
	}

	public static <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, NonNullConsumer<? super T> callback) {
		CallbackImpl<R, T> callbackImpl = new CallbackImpl<>(registry, id, callback);

		Either<List<CallbackImpl<?, ?>>, CreateRegistrate> either;
		synchronized (CALLBACKS) {
			either = CALLBACKS.computeIfAbsent(id.getNamespace(),
				k -> Either.left(new ArrayList<>()));
			// must be synchronized here, because if `registerRegistrate` were called between these two calls,
			// we would be adding to a list that would never be used
			either.ifLeft(callbacks -> callbacks.add(callbackImpl));
		}

		// This is safe to call outside the synchronized block, because a registrate will only ever be added once.
		either.ifRight(callbackImpl::addToRegistrate);
	}

	private record CallbackImpl<R, T extends R>(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, NonNullConsumer<? super T> callback) {
		// Helper method so javac doesn't explode on the generic types.
		// Otherwise, IntelliJ does type inference better than javac,
		// and everything becomes illegible with generic erasure casts.
		public void addToRegistrate(CreateRegistrate registrate) {
			registrate.<R, T>addRegisterCallback(id.getPath(), registry, callback);
		}
	}
}
