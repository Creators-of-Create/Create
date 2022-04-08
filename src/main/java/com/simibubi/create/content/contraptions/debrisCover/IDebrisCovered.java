package com.simibubi.create.content.contraptions.debrisCover;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IDebrisCovered {
	default boolean canBlockBeCovered(BlockState blockState) {
		return false;
	}
	default Iterable<BlockPos> getNeighbours() {
		return List.of();
	}

	DebrisCoverHandler.SelectionMode toggleCovered();
	void setCovered(DebrisCoverHandler.SelectionMode lockingState);
	boolean isCovered();
}
