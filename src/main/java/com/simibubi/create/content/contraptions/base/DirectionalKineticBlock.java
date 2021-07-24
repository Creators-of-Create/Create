package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public abstract class DirectionalKineticBlock extends KineticBlock {

	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public DirectionalKineticBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
		super.createBlockStateDefinition(builder);
	}

	public Direction getPreferredFacing(BlockItemUseContext context) {
		Direction prefferedSide = null;
		for (Direction side : Iterate.directions) {
			BlockState blockState = context.getLevel()
				.getBlockState(context.getClickedPos()
					.relative(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos()
					.relative(side), blockState, side.getOpposite()))
					if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
						prefferedSide = null;
						break;
					} else {
						prefferedSide = side;
					}
			}
		}
		return prefferedSide;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferred = getPreferredFacing(context);
		if (preferred == null || (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown())) {
			Direction nearestLookingDirection = context.getNearestLookingDirection();
			return defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer()
				.isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite());
		}
		return defaultBlockState().setValue(FACING, preferred.getOpposite());
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
