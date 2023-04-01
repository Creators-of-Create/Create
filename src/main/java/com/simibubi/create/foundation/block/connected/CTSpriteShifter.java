package com.simibubi.create.foundation.block.connected;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class CTSpriteShifter {

	private static final Map<String, SpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

	public static CTSpriteShiftEntry getCT(CTType type, ResourceLocation blockTexture, ResourceLocation connectedTexture) {
		String key = blockTexture + "->" + connectedTexture + "+" + type.getId();
		if (ENTRY_CACHE.containsKey(key))
			return (CTSpriteShiftEntry) ENTRY_CACHE.get(key);

		CTSpriteShiftEntry entry = new CTSpriteShiftEntry(type);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> entry.set(blockTexture, connectedTexture));
		ENTRY_CACHE.put(key, entry);
		return entry;
	}

}
