package com.simibubi.create.foundation.block.render;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface MultiPosDestructionHandler {
	/**
	 * Returned set must be mutable and must not be changed after it is returned.
	 */
	@Nullable
	Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState blockState, int progress);
}
