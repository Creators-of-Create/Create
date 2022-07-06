package com.simibubi.create.content.contraptions.components.crafter;

import static com.simibubi.create.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CrafterCTBehaviour extends ConnectedTextureBehaviour.Base {

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		if (state.getBlock() != other.getBlock())
			return false;
		if (state.getValue(HORIZONTAL_FACING) != other.getValue(HORIZONTAL_FACING))
			return false;

		ConnectedInput input1 = CrafterHelper.getInput(reader, pos);
		ConnectedInput input2 = CrafterHelper.getInput(reader, otherPos);

		if (input1 == null || input2 == null)
			return false;
		if (input1.data.isEmpty() || input2.data.isEmpty())
			return false;
		try {
			if (pos.offset(input1.data.get(0))
					.equals(otherPos.offset(input2.data.get(0))))
				return true;
		} catch (IndexOutOfBoundsException e) {
			// race condition. data somehow becomes empty between the last 2 if statements
		}

		return false;
	}

	@Override
	protected boolean reverseUVs(BlockState state, Direction direction) {
		if (!direction.getAxis()
			.isVertical())
			return false;
		Direction facing = state.getValue(HORIZONTAL_FACING);
		if (facing.getAxis() == direction.getAxis())
			return false;

		boolean isNegative = facing.getAxisDirection() == AxisDirection.NEGATIVE;
		if (direction == Direction.DOWN && facing.getAxis() == Axis.Z)
			return !isNegative;
		return isNegative;
	}

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
		Direction facing = state.getValue(HORIZONTAL_FACING);
		boolean isFront = facing.getAxis() == direction.getAxis();
		boolean isVertical = direction.getAxis()
			.isVertical();
		boolean facingX = facing.getAxis() == Axis.X;
		return isFront ? AllSpriteShifts.CRAFTER_FRONT
			: isVertical && !facingX ? AllSpriteShifts.CRAFTER_OTHERSIDE : AllSpriteShifts.CRAFTER_SIDE;
	}

}
