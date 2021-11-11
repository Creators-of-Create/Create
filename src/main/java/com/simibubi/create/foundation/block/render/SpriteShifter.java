package com.simibubi.create.foundation.block.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.simibubi.create.Create;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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

	@SubscribeEvent
	public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
		if (!event.getMap()
			.location()
			.equals(InventoryMenu.BLOCK_ATLAS))
			return;

		getAllTargetSprites()
			.forEach(event::addSprite);
	}

	@SubscribeEvent
	public static void onTextureStitchPost(TextureStitchEvent.Post event) {
		if (!event.getMap()
			.location()
			.equals(InventoryMenu.BLOCK_ATLAS))
			return;

		TextureAtlas atlas = event.getMap();
		for (SpriteShiftEntry entry : ENTRY_CACHE.values()) {
			entry.loadTextures(atlas);
		}
	}

}
