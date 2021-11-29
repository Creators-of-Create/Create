package com.simibubi.create.foundation.block.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface DestroyProgressRenderingHandler {
	/**
	 * Called before the default block breaking progress overlay is rendered.
	 *
	 * @return if the default rendering should be cancelled or not
	 */
	boolean renderDestroyProgress(ClientLevel level, LevelRenderer renderer, int breakerId, BlockPos pos, int progress, BlockState blockState);
}
