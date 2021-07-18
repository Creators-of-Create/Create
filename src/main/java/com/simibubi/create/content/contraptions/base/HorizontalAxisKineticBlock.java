package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public abstract class HorizontalAxisKineticBlock extends KineticBlock {

	public static final Property<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	public HorizontalAxisKineticBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_AXIS);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Axis preferredAxis = getPreferredHorizontalAxis(context);
		if (preferredAxis != null)
			return this.defaultBlockState().setValue(HORIZONTAL_AXIS, preferredAxis);
		return this.defaultBlockState().setValue(HORIZONTAL_AXIS, context.getHorizontalDirection().getClockWise().getAxis());
	}

	public static Axis getPreferredHorizontalAxis(BlockItemUseContext context) {
		Direction prefferedSide = null;
		for (Direction side : Iterate.horizontalDirections) {
			BlockState blockState = context.getLevel().getBlockState(context.getClickedPos().relative(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos().relative(side),
						blockState, side.getOpposite()))
					if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
						prefferedSide = null;
						break;
					} else {
						prefferedSide = side;
					}
			}
		}
		return prefferedSide == null ? null : prefferedSide.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_AXIS);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(HORIZONTAL_AXIS);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		Axis axis = state.getValue(HORIZONTAL_AXIS);
		return state.setValue(HORIZONTAL_AXIS,
				rot.rotate(Direction.get(AxisDirection.POSITIVE, axis)).getAxis());
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state;
	}

}
