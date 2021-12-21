package com.simibubi.create.lib.util;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class StickinessUtil {
	public static boolean canStickTo(BlockState state, BlockState other) {
		if (state.getBlock() == Blocks.HONEY_BLOCK && other.getBlock() == Blocks.SLIME_BLOCK) return false;
		if (state.getBlock() == Blocks.SLIME_BLOCK && other.getBlock() == Blocks.HONEY_BLOCK) return false;
		return (state.getBlock() == Blocks.SLIME_BLOCK || state.getBlock() == Blocks.HONEY_BLOCK) ||
				(other.getBlock() == Blocks.SLIME_BLOCK || other.getBlock() == Blocks.HONEY_BLOCK);
	}
}
