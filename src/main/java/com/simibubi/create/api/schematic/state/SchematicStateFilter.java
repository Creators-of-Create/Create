package com.simibubi.create.api.schematic.state;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface SchematicStateFilter {
	/**
	 * This will always be called from the logical server
	 */
	BlockState filterStates(@Nullable BlockEntity be, BlockState state);
}
