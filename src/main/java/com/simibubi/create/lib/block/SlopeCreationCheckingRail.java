package com.simibubi.create.lib.block;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface SlopeCreationCheckingRail {
	public boolean canMakeSlopes(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos);
}
