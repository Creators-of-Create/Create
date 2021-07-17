package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;

public abstract class RotatedPillarKineticBlock extends KineticBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public RotatedPillarKineticBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
			.setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		switch (rot) {
		case COUNTERCLOCKWISE_90:
		case CLOCKWISE_90:
			switch (state.getValue(AXIS)) {
			case X:
				return state.setValue(AXIS, Direction.Axis.Z);
			case Z:
				return state.setValue(AXIS, Direction.Axis.X);
			default:
				return state;
			}
		default:
			return state;
		}
	}

	public static Axis getPreferredAxis(BlockItemUseContext context) {
		Axis prefferedAxis = null;
		for (Direction side : Iterate.directions) {
			BlockState blockState = context.getLevel()
				.getBlockState(context.getClickedPos()
					.relative(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos()
					.relative(side), blockState, side.getOpposite()))
					if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
						prefferedAxis = null;
						break;
					} else {
						prefferedAxis = side.getAxis();
					}
			}
		}
		return prefferedAxis;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Axis preferredAxis = getPreferredAxis(context);
		if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer()
			.isShiftKeyDown()))
			return this.defaultBlockState()
				.setValue(AXIS, preferredAxis);
		return this.defaultBlockState()
			.setValue(AXIS, preferredAxis != null && context.getPlayer()
				.isShiftKeyDown() ? context.getClickedFace()
					.getAxis()
					: context.getNearestLookingDirection()
						.getAxis());
	}
}
