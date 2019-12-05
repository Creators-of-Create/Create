package com.simibubi.create.foundation.block;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class SpriteShifter {

	public static class SpriteShiftEntry {
		ResourceLocation originalTextureLocation;
		ResourceLocation targetTextureLocation;
		TextureAtlasSprite original;
		TextureAtlasSprite target;

		void loadTextures() {
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

	static Map<String, SpriteShiftEntry> textures = new HashMap<>();

	public static SpriteShiftEntry getCT(String blockId) {
		return get("block/" + blockId, "block/connected/" + blockId);
	}

	public static SpriteShiftEntry get(String originalLocation, String targetLocation) {
		String key = originalLocation + "->" + targetLocation;
		if (textures.containsKey(key))
			return textures.get(key);

		SpriteShiftEntry entry = new SpriteShiftEntry();
		entry.originalTextureLocation = new ResourceLocation(Create.ID, originalLocation);
		entry.targetTextureLocation = new ResourceLocation(Create.ID, targetLocation);
		textures.put(key, entry);
		return entry;
	}

	public static void reloadUVs() {
		textures.values().forEach(SpriteShiftEntry::loadTextures);
	}

}
