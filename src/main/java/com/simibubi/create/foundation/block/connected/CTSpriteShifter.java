package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.render.SpriteShifter;

import net.minecraft.util.ResourceLocation;

public class CTSpriteShifter extends SpriteShifter {

	public enum CTType {
		OMNIDIRECTIONAL, HORIZONTAL, VERTICAL;
	}

	public static CTSpriteShiftEntry get(CTType type, String blockTextureName) {
		return get(type, blockTextureName, blockTextureName);
	}
	
	
	public static CTSpriteShiftEntry get(CTType type, String blockTextureName, String connectedTextureName) {
		return get(type, new ResourceLocation(Create.ID, "block/" + blockTextureName), connectedTextureName);
	}
	
	public static CTSpriteShiftEntry get(CTType type, ResourceLocation blockTexture, String connectedTextureName) {
		String targetLocation = "block/connected/" + connectedTextureName;
		String key = type.name() + ":" + blockTexture.getNamespace() + ":" + blockTexture.getPath() + "->" + targetLocation;
		if (textures.containsKey(key))
			return (CTSpriteShiftEntry) textures.get(key);

		CTSpriteShiftEntry entry = create(type);
		ResourceLocation targetTextureLocation = new ResourceLocation(Create.ID, targetLocation);
		entry.set(blockTexture, targetTextureLocation);

		textures.put(key, entry);
		return entry;
	}

	private static CTSpriteShiftEntry create(CTType type) {
		switch (type) {
		case HORIZONTAL:
			return new CTSpriteShiftEntry.Horizontal();
		case OMNIDIRECTIONAL:
			return new CTSpriteShiftEntry.Omnidirectional();
		case VERTICAL:
			return new CTSpriteShiftEntry.Vertical();
		default:
			return null;
		}
	}

}
