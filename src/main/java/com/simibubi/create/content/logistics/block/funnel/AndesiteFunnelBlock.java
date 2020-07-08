package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class AndesiteFunnelBlock extends FunnelBlock {

	public AndesiteFunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public BlockState getEquivalentBeltFunnel(BlockState state) {
		Direction facing = state.get(FACING);
		return AllBlocks.ANDESITE_BELT_FUNNEL.getDefaultState()
			.with(BeltFunnelBlock.HORIZONTAL_FACING, facing);
	}
	
	@Override
	public BlockState getEquivalentChuteFunnel(BlockState state) {
		Direction facing = state.get(FACING);
		return AllBlocks.ANDESITE_CHUTE_FUNNEL.getDefaultState()
			.with(ChuteFunnelBlock.HORIZONTAL_FACING, facing);
	}

}
