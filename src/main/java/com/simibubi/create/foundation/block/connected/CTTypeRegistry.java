package com.simibubi.create.foundation.block.connected;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

public class CTTypeRegistry {
	private static final Map<ResourceLocation, CTType> TYPES = new HashMap<>();

	public static void register(CTType type) {
		ResourceLocation id = type.getId();
		if (TYPES.containsKey(id))
			throw new IllegalArgumentException("Tried to override CTType registration for id '" + id + "'. This is not supported!");
		TYPES.put(id, type);
	}

	@Nullable
	public static CTType get(ResourceLocation id) {
		return TYPES.get(id);
	}
}
