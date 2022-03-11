package com.simibubi.create.content.contraptions.debrisShield;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IDebrisShielded {
	default boolean canBlockBeShielded(BlockState blockState) {
		return false;
	}
	default Iterable<BlockPos> getNeighbours() {
		return List.of();
	}

	DebrisShieldHandler.SelectionMode toggleShielded();
	void setShielded(DebrisShieldHandler.SelectionMode lockingState);
	boolean isShielded();
}
