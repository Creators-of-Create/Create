package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.render.SpriteShifter;

import net.minecraft.util.ResourceLocation;

public class CTSpriteShifter extends SpriteShifter {

	public enum CTType {
		OMNIDIRECTIONAL, HORIZONTAL, VERTICAL, CROSS;
	}

	public static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
		return getCT(type, blockTextureName, blockTextureName);
	}

	public static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
		return getCT(type, new ResourceLocation(Create.ID, "block/" + blockTextureName), connectedTextureName);
	}

	public static CTSpriteShiftEntry getCT(CTType type, ResourceLocation blockTexture, String connectedTextureName) {
		String targetLocation = "block/" + connectedTextureName + "_connected";
		String key =
			type.name() + ":" + blockTexture.getNamespace() + ":" + blockTexture.getPath() + "->" + targetLocation;
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
		case CROSS:
			return new CTSpriteShiftEntry.Cross();
		default:
			return null;
		}
	}

}
