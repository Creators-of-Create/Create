package com.simibubi.create.foundation.block.render;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class SpriteShiftEntry {
	protected ResourceLocation originalTextureLocation;
	protected ResourceLocation targetTextureLocation;
	protected TextureAtlasSprite original;
	protected TextureAtlasSprite target;

	public void set(ResourceLocation originalTextureLocation, ResourceLocation targetTextureLocation) {
		this.originalTextureLocation = originalTextureLocation;
		this.targetTextureLocation = targetTextureLocation;
	}

	protected void loadTextures() {
		Function<ResourceLocation, TextureAtlasSprite> textureMap = Minecraft.getInstance()
			.getTextureAtlas(PlayerContainer.BLOCK_ATLAS);
		original = textureMap.apply(originalTextureLocation);
		target = textureMap.apply(targetTextureLocation);
	}

	public ResourceLocation getTargetResourceLocation() {
		return targetTextureLocation;
	}

	public TextureAtlasSprite getTarget() {
		if (target == null)
			loadTextures();
		return target;
	}

	public TextureAtlasSprite getOriginal() {
		if (original == null)
			loadTextures();
		return original;
	}
}