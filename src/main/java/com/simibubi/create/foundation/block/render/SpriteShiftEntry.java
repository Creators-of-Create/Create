package com.simibubi.create.foundation.block.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
		AtlasTexture textureMap = Minecraft.getInstance().getTextureMap();
		original = textureMap.getSprite(originalTextureLocation);
		target = textureMap.getSprite(targetTextureLocation);
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