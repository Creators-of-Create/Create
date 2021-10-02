package com.simibubi.create.foundation.block.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.simibubi.create.Create;

import net.minecraft.util.ResourceLocation;

public class SpriteShifter {

	protected static final Map<String, SpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

	public static SpriteShiftEntry get(ResourceLocation originalLocation, ResourceLocation targetLocation) {
		String key = originalLocation + "->" + targetLocation;
		if (ENTRY_CACHE.containsKey(key))
			return ENTRY_CACHE.get(key);

		SpriteShiftEntry entry = new SpriteShiftEntry();
		entry.set(originalLocation, targetLocation);
		ENTRY_CACHE.put(key, entry);
		return entry;
	}

	public static SpriteShiftEntry get(String originalLocation, String targetLocation) {
		return get(Create.asResource(originalLocation), Create.asResource(targetLocation));
	}

	public static void reloadUVs() {
		ENTRY_CACHE.values().forEach(SpriteShiftEntry::loadTextures);
	}

	public static List<ResourceLocation> getAllTargetSprites() {
		return ENTRY_CACHE.values().stream().map(SpriteShiftEntry::getTargetResourceLocation).collect(Collectors.toList());
	}

}
