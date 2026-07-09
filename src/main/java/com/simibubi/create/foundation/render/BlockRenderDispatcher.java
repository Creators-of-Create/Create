package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.model.BakedModel;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Temporary Create 26.2 porting bridge for legacy block model callers.
 * TODO 26.2: Replace with BlockModelResolver/BlockStateModelSet.
 */
@Deprecated(forRemoval = true)
public class BlockRenderDispatcher {
	private static final BakedModel EMPTY_MODEL = new BakedModel() {};

	public BakedModel getBlockModel(BlockState state) {
		return EMPTY_MODEL;
	}

	public ModelBlockRenderer getModelRenderer() {
		return null;
	}
}
