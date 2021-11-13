package com.simibubi.create.lib.utility;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PlantUtil {
	public static boolean isPlant(Block block) {
		return getPlant(block) != null;
	}

	@Nullable
	public static BlockState getPlant(Block block) {
		if (block instanceof CropBlock || block instanceof SaplingBlock || block instanceof FlowerBlock ||
			block == Blocks.DEAD_BUSH || block == Blocks.LILY_PAD || block == Blocks.RED_MUSHROOM ||
			block == Blocks.BROWN_MUSHROOM || block == Blocks.NETHER_WART || block == Blocks.TALL_GRASS) {
			return block.defaultBlockState();
		} else {
			return null;
		}
	}
}
