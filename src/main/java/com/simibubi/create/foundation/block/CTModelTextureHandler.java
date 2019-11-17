package com.simibubi.create.foundation.block;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class CTModelTextureHandler {

	static class TextureEntry {
		ResourceLocation originalTextureLocation;
		ResourceLocation connectedTextureLocation;
		TextureAtlasSprite originalTexture;
		TextureAtlasSprite connectedTextures;

		void loadTextures() {
			AtlasTexture textureMap = Minecraft.getInstance().getTextureMap();
			originalTexture = textureMap.getSprite(originalTextureLocation);
			connectedTextures = textureMap.getSprite(connectedTextureLocation);
		}
	}

	static Map<String, TextureEntry> textures = new HashMap<>();

	public static TextureEntry get(String blockId) {
		if (textures.containsKey(blockId))
			return textures.get(blockId);

		TextureEntry entry = new TextureEntry();
		entry.originalTextureLocation = new ResourceLocation(Create.ID, "block/" + blockId);
		entry.connectedTextureLocation = new ResourceLocation(Create.ID, "block/connected/" + blockId);
		entry.loadTextures();
		textures.put(blockId, entry);
		return entry;
	}

	public static void reloadUVs() {
		textures.values().forEach(TextureEntry::loadTextures);
	}

}
