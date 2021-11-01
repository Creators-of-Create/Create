package com.simibubi.create.foundation.block.render;

import java.util.Arrays;

import net.minecraft.client.renderer.block.model.BakedQuad;

public final class QuadHelper {

	private QuadHelper() {}

	public static BakedQuad clone(BakedQuad quad) {
		return new BakedQuad(Arrays.copyOf(quad.getVertices(), quad.getVertices().length),
			quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
	}

}
