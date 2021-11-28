package com.simibubi.create.foundation.block.render;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import com.simibubi.create.lib.utility.TextureStitchUtil;

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

}
