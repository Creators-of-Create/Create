package com.simibubi.create.foundation.block.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.simibubi.create.Create;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

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

	public static List<ResourceLocation> getAllTargetSprites() {
		return ENTRY_CACHE.values().stream().map(SpriteShiftEntry::getTargetResourceLocation).collect(Collectors.toList());
	}

	public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
		if (!event.getMap()
			.location()
			.equals(PlayerContainer.BLOCK_ATLAS))
			return;

		getAllTargetSprites()
			.forEach(event::addSprite);
	}

	public static void onTextureStitchPost(TextureStitchEvent.Post event) {
		if (!event.getMap()
			.location()
			.equals(PlayerContainer.BLOCK_ATLAS))
			return;

		AtlasTexture atlas = event.getMap();
		for (SpriteShiftEntry entry : ENTRY_CACHE.values()) {
			entry.loadTextures(atlas);
		}
	}

}
