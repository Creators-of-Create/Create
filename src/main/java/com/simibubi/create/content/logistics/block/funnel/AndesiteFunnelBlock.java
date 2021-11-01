package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllBlocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class AndesiteFunnelBlock extends FunnelBlock {

	public AndesiteFunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public BlockState getEquivalentBeltFunnel(BlockGetter world, BlockPos pos, BlockState state) {
		Direction facing = getFunnelFacing(state);
		return AllBlocks.ANDESITE_BELT_FUNNEL.getDefaultState()
			.setValue(BeltFunnelBlock.HORIZONTAL_FACING, facing)
			.setValue(POWERED, state.getValue(POWERED));
	}

}
