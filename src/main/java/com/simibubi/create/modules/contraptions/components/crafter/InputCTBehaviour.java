package com.simibubi.create.modules.contraptions.components.crafter;

import static com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.Arrays;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.modules.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

public class InputCTBehaviour extends ConnectedTextureBehaviour {

	static final CTSpriteShiftEntry front = CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, "crafter_top", "brass_casing");
	static final CTSpriteShiftEntry side = CTSpriteShifter.get(CTType.VERTICAL, "crafter_side");
	static final CTSpriteShiftEntry otherSide = CTSpriteShifter.get(CTType.HORIZONTAL, "crafter_side");

	@Override
	public boolean connectsTo(BlockState state, BlockState other, IEnviromentBlockReader reader, BlockPos pos,
			BlockPos otherPos, Direction face) {
		if (state.getBlock() != other.getBlock())
			return false;
		if (state.get(HORIZONTAL_FACING) != other.get(HORIZONTAL_FACING))
			return false;

		ConnectedInput input1 = CrafterHelper.getInput(reader, pos);
		ConnectedInput input2 = CrafterHelper.getInput(reader, otherPos);

		if (input1 == null || input2 == null)
			return false;
		if (pos.add(input1.data.get(0)).equals(otherPos.add(input2.data.get(0))))
			return true;

		return false;
	}

	@Override
	protected boolean shouldFlipUVs(BlockState state, Direction direction) {
		if (!direction.getAxis().isVertical())
			return false;
		Direction facing = state.get(HORIZONTAL_FACING);
		if (facing.getAxis() == direction.getAxis())
			return false;

		boolean isNegative = facing.getAxisDirection() == AxisDirection.NEGATIVE;
		if (direction == Direction.DOWN && facing.getAxis() == Axis.Z)
			return !isNegative;
		return isNegative;
	}

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		Direction facing = state.get(HORIZONTAL_FACING);
		boolean isFront = facing.getAxis() == direction.getAxis();
		boolean isVertical = direction.getAxis().isVertical();
		boolean facingX = facing.getAxis() == Axis.X;
		return isFront ? front : isVertical && !facingX ? otherSide : side;
	}

	@Override
	public Iterable<CTSpriteShiftEntry> getAllCTShifts() {
		return Arrays.asList(front, side);
	}

}
