package com.jozufozu.flywheel.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class StitchedSprite {

	private final ResourceLocation loc;

	TextureAtlasSprite sprite;

	public StitchedSprite(ResourceLocation loc) {
		this.loc = loc;
	}

	public ResourceLocation getLoc() {
		return loc;
	}

	public TextureAtlasSprite getSprite() {
		if (sprite == null) {
			sprite = Minecraft.getInstance()
					.getSpriteAtlas(PlayerContainer.BLOCK_ATLAS_TEXTURE)
					.apply(loc);
		}

		return sprite;
	}
}
