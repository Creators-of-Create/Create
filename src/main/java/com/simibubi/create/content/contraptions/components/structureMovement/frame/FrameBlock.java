package com.simibubi.create.content.contraptions.components.structureMovement.frame;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class FrameBlock extends Block {
	public FrameBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public boolean canStickTo(BlockState state, BlockState other) {
		return other.getBlock() instanceof FrameBlock;
	}
}
