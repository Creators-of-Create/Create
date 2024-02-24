package com.simibubi.create.foundation.utility;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Components {
	private static final Component IMMUTABLE_EMPTY = Component.empty();

	public static Component immutableEmpty() {
		return IMMUTABLE_EMPTY;
	}

	/** Use {@link #immutableEmpty()} when possible to prevent creating an extra object. */
	public static MutableComponent empty() {
		return Component.empty();
	}

	public static MutableComponent literal(String str) {
		return Component.literal(str);
	}

	public static MutableComponent translatable(String key) {
		return Component.translatable(key);
	}

	public static MutableComponent translatable(String key, Object... args) {
		return Component.translatable(key, args);
	}

	public static MutableComponent keybind(String name) {
		return Component.keybind(name);
	}
}
