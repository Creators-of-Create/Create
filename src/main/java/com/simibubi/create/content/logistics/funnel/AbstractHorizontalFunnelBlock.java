package com.simibubi.create.content.logistics.funnel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

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
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
		return p_185471_1_.rotate(p_185471_2_.getRotation(p_185471_1_.getValue(HORIZONTAL_FACING)));
	}

}
