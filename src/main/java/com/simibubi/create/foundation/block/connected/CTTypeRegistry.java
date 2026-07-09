package com.simibubi.create.foundation.block.connected;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.Identifier;

public class CTTypeRegistry {
	private static final Map<Identifier, CTType> TYPES = new HashMap<>();

	public static void register(CTType type) {
		Identifier id = type.getId();
		if (TYPES.containsKey(id))
			throw new IllegalArgumentException("Tried to override CTType registration for id '" + id + "'. This is not supported!");
		TYPES.put(id, type);
	}

	@Nullable
	public static CTType get(Identifier id) {
		return TYPES.get(id);
	}
}
