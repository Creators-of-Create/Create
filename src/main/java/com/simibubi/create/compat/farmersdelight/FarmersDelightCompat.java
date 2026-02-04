package com.simibubi.create.compat.farmersdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockState;

import vectorwing.farmersdelight.common.registry.ModBlocks;

public class FarmersDelightCompat {
	public static boolean shouldHarvestMushroom(Level world, BlockPos pos, BlockState state) {
		return !world.getBlockState(pos.below()).is(ModBlocks.RICH_SOIL.get());
	}
}
