package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.BakedQuadAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public class BakedQuadHelper {
	public static TextureAtlasSprite getSprite(BakedQuad quad) {
		return get(quad).create$sprite();
	}

	private static BakedQuadAccessor get(BakedQuad quad) {
		return MixinHelper.cast(quad);
	}

	private BakedQuadHelper() {}
}
