package com.simibubi.create.foundation.block;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.DirectionHelper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class WrenchableDirectionalBlock extends DirectionalBlock implements IWrenchable {

	public WrenchableDirectionalBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		Direction facing = originalState.getValue(FACING);

		if (facing.getAxis() == targetedFace.getAxis())
			return originalState;

		Direction newFacing = DirectionHelper.rotateAround(facing, targetedFace.getAxis());

		return originalState.setValue(FACING, newFacing);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

}
