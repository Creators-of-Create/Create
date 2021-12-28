package com.simibubi.create.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface CustomLightEmissionBlock {
	int getLightEmission(BlockState state, BlockGetter world, BlockPos pos);
}
