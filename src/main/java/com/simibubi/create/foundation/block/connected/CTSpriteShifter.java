package com.simibubi.create.foundation.block.connected;

import java.util.HashMap;
import java.util.Map;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;

public class CTSpriteShifter {

	private static final Map<String, CTSpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

	public static CTSpriteShiftEntry getCT(CTType type, ResourceLocation blockTexture, ResourceLocation connectedTexture) {
		String key = blockTexture + "->" + connectedTexture + "+" + type.getId();
		if (ENTRY_CACHE.containsKey(key))
			return ENTRY_CACHE.get(key);

		CTSpriteShiftEntry entry = new CTSpriteShiftEntry(type);
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> entry.set(blockTexture, connectedTexture));
		ENTRY_CACHE.put(key, entry);
		return entry;
	}

}
