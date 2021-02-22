package com.simibubi.create.content.logistics.block.funnel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class AbstractDirectionalFunnelBlock extends AbstractFunnelBlock {

	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	
	protected AbstractDirectionalFunnelBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(FACING));
	}
	
	@Override
	protected Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

}
