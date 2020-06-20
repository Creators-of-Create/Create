package com.simibubi.create.content.logistics.block.chute;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;

public class ChutePortBlock extends HorizontalBlock {

	public ChutePortBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(HORIZONTAL_FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
		return getDefaultState().with(HORIZONTAL_FACING, p_196258_1_.getPlacementHorizontalFacing());
	}

}
