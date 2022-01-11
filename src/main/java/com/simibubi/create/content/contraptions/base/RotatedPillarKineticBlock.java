package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class RotatedPillarKineticBlock extends KineticBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public RotatedPillarKineticBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
			.setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return switch (rot) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
				case X -> state.setValue(AXIS, Axis.Z);
				case Z -> state.setValue(AXIS, Axis.X);
				default -> state;
			};
			default -> state;
		};
	}

	public static Axis getPreferredAxis(BlockPlaceContext context) {
		Axis preferredAxis = null;
		for (Direction side : Iterate.directions) {
			BlockState blockState = context.getLevel()
				.getBlockState(context.getClickedPos()
					.relative(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(
						blockState, side.getOpposite(), context.getLevel(), context.getClickedPos()))
					if (preferredAxis != null && preferredAxis != side.getAxis()) {
						preferredAxis = null;
						break;
					} else {
						preferredAxis = side.getAxis();
					}
			}
		}
		return preferredAxis;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
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
