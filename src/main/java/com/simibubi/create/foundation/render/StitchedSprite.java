package com.simibubi.create.foundation.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.TextureStitchEvent;

public class StitchedSprite {
	private static final Map<ResourceLocation, List<StitchedSprite>> ALL = new HashMap<>();

	protected final ResourceLocation atlasLocation;
	protected final ResourceLocation location;
	protected TextureAtlasSprite sprite;

	public StitchedSprite(ResourceLocation atlas, ResourceLocation location) {
		atlasLocation = atlas;
		this.location = location;
		ALL.computeIfAbsent(atlasLocation, $ -> new ArrayList<>()).add(this);
	}

	public StitchedSprite(ResourceLocation location) {
		this(InventoryMenu.BLOCK_ATLAS, location);
	}

	public static void onTextureStitchPost(TextureStitchEvent.Post event) {
		TextureAtlas atlas = event.getAtlas();
		ResourceLocation atlasLocation = atlas.location();
		List<StitchedSprite> sprites = ALL.get(atlasLocation);
		if (sprites != null) {
			for (StitchedSprite sprite : sprites) {
				sprite.loadSprite(atlas);
			}
		}
	}

	protected void loadSprite(TextureAtlas atlas) {
		sprite = atlas.getSprite(location);
	}

	public ResourceLocation getAtlasLocation() {
		return atlasLocation;
	}

	public ResourceLocation getLocation() {
		return location;
	}

	public TextureAtlasSprite get() {
		return sprite;
	}
}
