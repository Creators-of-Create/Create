package com.simibubi.create.content.logistics.block.funnel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import net.minecraft.block.AbstractBlock.Properties;

public abstract class AbstractHorizontalFunnelBlock extends AbstractFunnelBlock {

	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	protected AbstractHorizontalFunnelBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(HORIZONTAL_FACING));
	}

	@Override
	protected Direction getFacing(BlockState state) {
		return state.getValue(HORIZONTAL_FACING);
	}
	
	@Override
	public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
		return p_185499_1_.setValue(HORIZONTAL_FACING, p_185499_2_.rotate(p_185499_1_.getValue(HORIZONTAL_FACING)));
	}

	@Override
	public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
		return p_185471_1_.rotate(p_185471_2_.getRotation(p_185471_1_.getValue(HORIZONTAL_FACING)));
	}

}
