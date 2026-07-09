package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.BlockRenderDispatcher;

/**
 * Temporary Create 26.2 porting bridge for call sites still using the old block renderer facade.
 * TODO 26.2: Replace with BlockModelResolver, BlockStateModelSet, and render-state submission.
 */
@Deprecated(forRemoval = true)
public class LegacyBlockRendererBridge {
	private static final BlockRenderDispatcher RENDERER = new BlockRenderDispatcher();

	public static BlockRenderDispatcher getBlockRenderer() {
		return RENDERER;
	}
}
