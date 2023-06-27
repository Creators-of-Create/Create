package com.simibubi.create.foundation.block.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class SpriteShifter {

	private static final Map<String, SpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

	public static SpriteShiftEntry get(ResourceLocation originalLocation, ResourceLocation targetLocation) {
		String key = originalLocation + "->" + targetLocation;
		if (ENTRY_CACHE.containsKey(key))
			return ENTRY_CACHE.get(key);

		SpriteShiftEntry entry = new SpriteShiftEntry();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> entry.set(originalLocation, targetLocation));
		ENTRY_CACHE.put(key, entry);
		return entry;
	}

}
