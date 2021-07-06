package com.simibubi.create.foundation.block.render;

import java.util.Arrays;

import net.minecraft.client.renderer.model.BakedQuad;

public final class QuadHelper {

	private QuadHelper() {}

	public static BakedQuad clone(BakedQuad quad) {
		return new BakedQuad(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length),
			quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.hasShade());
	}

}
