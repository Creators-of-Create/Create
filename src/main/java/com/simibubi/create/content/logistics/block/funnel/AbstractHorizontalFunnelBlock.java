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
	public BlockState rotate(BlockState pState, Rotation pRot) {
		return pState.setValue(HORIZONTAL_FACING, pRot.rotate(pState.getValue(HORIZONTAL_FACING)));
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirrorIn) {
		return pState.rotate(pMirrorIn.getRotation(pState.getValue(HORIZONTAL_FACING)));
	}

}
