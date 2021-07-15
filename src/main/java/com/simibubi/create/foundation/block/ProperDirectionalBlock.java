package com.simibubi.create.foundation.block;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.DirectionHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import net.minecraft.block.AbstractBlock.Properties;

public class ProperDirectionalBlock extends DirectionalBlock implements IWrenchable {

	public ProperDirectionalBlock(Properties p_i48415_1_) {
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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
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
