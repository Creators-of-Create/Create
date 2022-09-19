package com.simibubi.create.foundation.block.connected;

import net.createmod.catnip.render.SpriteShifter;
import net.minecraft.resources.ResourceLocation;

public class CTSpriteShifter extends SpriteShifter {

	//private static final Map<String, SpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

	public static CTSpriteShiftEntry getCT(CTType type, ResourceLocation blockTexture, ResourceLocation connectedTexture) {
		String key = blockTexture + "->" + connectedTexture + "+" + type.getId();
		if (ENTRY_CACHE.containsKey(key))
			return (CTSpriteShiftEntry) ENTRY_CACHE.get(key);

		CTSpriteShiftEntry entry = new CTSpriteShiftEntry(type, blockTexture, connectedTexture);
		ENTRY_CACHE.put(key, entry);
		return entry;
	}

}
