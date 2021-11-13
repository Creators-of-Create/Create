package com.simibubi.create.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface WeakPowerCheckingBlock {
	boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction side);
}
