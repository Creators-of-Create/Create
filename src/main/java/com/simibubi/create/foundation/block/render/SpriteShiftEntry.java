package com.simibubi.create.foundation.block.render;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class SpriteShiftEntry {
	protected ResourceLocation originalTextureLocation;
	protected ResourceLocation targetTextureLocation;
	protected TextureAtlasSprite original;
	protected TextureAtlasSprite target;

	public void set(ResourceLocation originalTextureLocation, ResourceLocation targetTextureLocation) {
		this.originalTextureLocation = originalTextureLocation;
		this.targetTextureLocation = targetTextureLocation;
	}

	public ResourceLocation getOriginalResourceLocation() {
		return originalTextureLocation;
	}

	public ResourceLocation getTargetResourceLocation() {
		return targetTextureLocation;
	}

	public TextureAtlasSprite getOriginal() {
		return original;
	}

	public TextureAtlasSprite getTarget() {
		return target;
	}

	protected void loadTextures(TextureAtlas atlas) {
		original = atlas.getSprite(originalTextureLocation);
		target = atlas.getSprite(targetTextureLocation);
	}
}