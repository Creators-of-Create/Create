package com.simibubi.create.impl.registrate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class CreateRegistrateRegistrationCallbackImpl {
	private static final List<CallbackImpl<?>> CALLBACKS = new ArrayList<>();

	@UnmodifiableView
	public static final List<CallbackImpl<?>> CALLBACKS_VIEW = Collections.unmodifiableList(CALLBACKS);

	public static <T> void register(ResourceKey<? extends Registry<T>> registry, ResourceLocation id, Consumer<T> callback) {
		CALLBACKS.add(new CallbackImpl<>(registry, id, callback));
	}

	public record CallbackImpl<T>(ResourceKey<? extends Registry<T>> registry, ResourceLocation id,
								  Consumer<T> callback) {
	}
}
