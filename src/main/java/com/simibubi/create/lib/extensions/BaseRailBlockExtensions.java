package com.simibubi.create.lib.extensions;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public interface BaseRailBlockExtensions {
	RailShape create$getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @Nullable BaseRailBlock cart);

	RailShape create$getRailDirection(BlockState state);
}
