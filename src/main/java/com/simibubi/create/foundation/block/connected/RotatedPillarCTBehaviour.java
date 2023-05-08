package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.content.curiosities.frames.CopycatBlock;
import com.simibubi.create.content.palettes.ConnectedPillarBlock;
import com.simibubi.create.content.palettes.LayeredBlock;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class RotatedPillarCTBehaviour extends HorizontalCTBehaviour {

	public RotatedPillarCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		super(layerShift, topShift);
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
		if (other.getBlock() != state.getBlock())
			return false;
		Axis stateAxis = state.getValue(LayeredBlock.AXIS);
		if (other.getValue(LayeredBlock.AXIS) != stateAxis)
			return false;
		if (isBeingBlocked(state, reader, pos, otherPos, face))
			return false;
		if (reader.getBlockState(pos).getBlock() instanceof CopycatBlock)
			return true;
		if (reader.getBlockState(otherPos).getBlock() instanceof CopycatBlock)
			return true;
		if (primaryOffset != null && primaryOffset.getAxis() != stateAxis
			&& !ConnectedPillarBlock.getConnection(state, primaryOffset))
			return false;
		if (secondaryOffset != null && secondaryOffset.getAxis() != stateAxis) {
			if (!ConnectedPillarBlock.getConnection(state, secondaryOffset))
				return false;
			if (!ConnectedPillarBlock.getConnection(other, secondaryOffset.getOpposite()))
				return false;
		}
		return true;
	}

	@Override
	protected boolean isBeingBlocked(BlockState state, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		return state.getValue(LayeredBlock.AXIS) == face.getAxis()
			&& super.isBeingBlocked(state, reader, pos, otherPos, face);
	}

	@Override
	protected boolean reverseUVs(BlockState state, Direction face) {
		Axis axis = state.getValue(LayeredBlock.AXIS);
		if (axis == Axis.X)
			return face.getAxisDirection() == AxisDirection.NEGATIVE && face.getAxis() != Axis.X;
		if (axis == Axis.Z)
			return face != Direction.NORTH && face.getAxisDirection() != AxisDirection.POSITIVE;
		return super.reverseUVs(state, face);
	}

	@Override
	protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
		return super.reverseUVsHorizontally(state, face);
	}

	@Override
	protected boolean reverseUVsVertically(BlockState state, Direction face) {
		Axis axis = state.getValue(LayeredBlock.AXIS);
		if (axis == Axis.X && face == Direction.NORTH)
			return false;
		if (axis == Axis.Z && face == Direction.WEST)
			return false;
		return super.reverseUVsVertically(state, face);
	}

	@Override
	protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Axis axis = state.getValue(LayeredBlock.AXIS);
		if (axis == Axis.Y)
			return super.getUpDirection(reader, pos, state, face);
		boolean alongX = axis == Axis.X;
		if (face.getAxis()
			.isVertical() && alongX)
			return super.getUpDirection(reader, pos, state, face).getClockWise();
		if (face.getAxis() == axis || face.getAxis()
			.isVertical())
			return super.getUpDirection(reader, pos, state, face);
		return Direction.fromAxisAndDirection(axis, alongX ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
	}

	@Override
	protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Axis axis = state.getValue(LayeredBlock.AXIS);
		if (axis == Axis.Y)
			return super.getRightDirection(reader, pos, state, face);
		if (face.getAxis()
			.isVertical() && axis == Axis.X)
			return super.getRightDirection(reader, pos, state, face).getClockWise();
		if (face.getAxis() == axis || face.getAxis()
			.isVertical())
			return super.getRightDirection(reader, pos, state, face);
		return Direction.fromAxisAndDirection(Axis.Y, face.getAxisDirection());
	}

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
		return super.getShift(state,
			direction.getAxis() == state.getValue(LayeredBlock.AXIS) ? Direction.UP : Direction.SOUTH, sprite);
	}

}
