package com.simibubi.create.content.palettes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class MetalBlock extends Block {
	private final boolean isBeaconBaseBlock;

	public MetalBlock(Properties properties) {
		super(properties);
		isBeaconBaseBlock = false;
	}

	public MetalBlock(Properties properties, boolean isBeaconBaseBlock) {
		super(properties);
		this.isBeaconBaseBlock = isBeaconBaseBlock;
	}

	@Override
	public boolean isBeaconBase(BlockState state, IWorldReader world, BlockPos pos, BlockPos beacon) {
		return isBeaconBaseBlock ? true : super.isBeaconBase(state, world, pos, beacon);
	}
}
