package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public abstract class DirectionalAxisKineticBlock extends DirectionalKineticBlock {

	public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE = BooleanProperty.create("axis_along_first");

	public DirectionalAxisKineticBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(AXIS_ALONG_FIRST_COORDINATE);
		super.createBlockStateDefinition(builder);
	}

	protected Direction getFacingForPlacement(BlockItemUseContext context) {
		Direction facing = context.getNearestLookingDirection()
			.getOpposite();
		if (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown())
			facing = facing.getOpposite();
		return facing;
	}

	protected boolean getAxisAlignmentForPlacement(BlockItemUseContext context) {
		return context.getHorizontalDirection()
			.getAxis() == Axis.X;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = getFacingForPlacement(context);
		BlockPos pos = context.getClickedPos();
		World world = context.getLevel();
		boolean alongFirst = false;
		Axis faceAxis = facing.getAxis();

		if (faceAxis.isHorizontal()) {
			alongFirst = faceAxis == Axis.Z;
			Direction positivePerpendicular = DirectionHelper.getPositivePerpendicular(faceAxis);

			boolean shaftAbove = prefersConnectionTo(world, pos, Direction.UP, true);
			boolean shaftBelow = prefersConnectionTo(world, pos, Direction.DOWN, true);
			boolean preferLeft = prefersConnectionTo(world, pos, positivePerpendicular, false);
			boolean preferRight = prefersConnectionTo(world, pos, positivePerpendicular.getOpposite(), false);

			if (shaftAbove || shaftBelow || preferLeft || preferRight)
				alongFirst = faceAxis == Axis.X;
		}

		if (faceAxis.isVertical()) {
			alongFirst = getAxisAlignmentForPlacement(context);
			Direction prefferedSide = null;

			for (Direction side : Iterate.horizontalDirections) {
				if (!prefersConnectionTo(world, pos, side, true)
					&& !prefersConnectionTo(world, pos, side.getClockWise(), false))
					continue;
				if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
					prefferedSide = null;
					break;
				}
				prefferedSide = side;
			}

			if (prefferedSide != null)
				alongFirst = prefferedSide.getAxis() == Axis.X;
		}

		return this.defaultBlockState()
			.setValue(FACING, facing)
			.setValue(AXIS_ALONG_FIRST_COORDINATE, alongFirst);
	}

	protected boolean prefersConnectionTo(IWorldReader reader, BlockPos pos, Direction facing, boolean shaftAxis) {
		if (!shaftAxis)
			return false;
		BlockPos neighbourPos = pos.relative(facing);
		BlockState blockState = reader.getBlockState(neighbourPos);
		Block block = blockState.getBlock();
		return block instanceof IRotate
			&& ((IRotate) block).hasShaftTowards(reader, neighbourPos, blockState, facing.getOpposite());
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		Axis pistonAxis = state.getValue(FACING)
			.getAxis();
		boolean alongFirst = state.getValue(AXIS_ALONG_FIRST_COORDINATE);

		if (pistonAxis == Axis.X)
			return alongFirst ? Axis.Y : Axis.Z;
		if (pistonAxis == Axis.Y)
			return alongFirst ? Axis.X : Axis.Z;
		if (pistonAxis == Axis.Z)
			return alongFirst ? Axis.X : Axis.Y;

		throw new IllegalStateException("Unknown axis??");
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		if (rot.ordinal() % 2 == 1)
			state = state.cycle(AXIS_ALONG_FIRST_COORDINATE);
		return super.rotate(state, rot);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

}
