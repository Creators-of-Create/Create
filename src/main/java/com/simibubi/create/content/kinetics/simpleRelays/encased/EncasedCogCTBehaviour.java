package com.simibubi.create.content.kinetics.simpleRelays.encased;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedCogCTBehaviour extends EncasedCTBehaviour {

	private Couple<CTSpriteShiftEntry> sideShifts;
	private boolean large;

	public EncasedCogCTBehaviour(CTSpriteShiftEntry shift) {
		this(shift, null);
	}

	public EncasedCogCTBehaviour(CTSpriteShiftEntry shift, Couple<CTSpriteShiftEntry> sideShifts) {
		super(shift);
		large = sideShifts == null;
		this.sideShifts = sideShifts;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face) {
		Axis axis = state.getValue(AXIS);
		if (large || axis == face.getAxis())
			return super.connectsTo(state, other, reader, pos, otherPos, face);

		if (other.getBlock() == state.getBlock() && other.getValue(AXIS) == state.getValue(AXIS))
			return true;

		BlockState blockState = reader.getBlockState(otherPos.relative(face));
		if (!ICogWheel.isLargeCog(blockState))
			return false;

		return ((IRotate) blockState.getBlock()).getRotationAxis(blockState) == axis;
	}

	@Override
	protected boolean reverseUVs(BlockState state, Direction face) {
		return state.getValue(AXIS)
			.isHorizontal()
			&& face.getAxis()
				.isHorizontal()
			&& face.getAxisDirection() == AxisDirection.POSITIVE;
	}

	@Override
	protected boolean reverseUVsVertically(BlockState state, Direction face) {
		if (!large && state.getValue(AXIS) == Axis.X && face.getAxis() == Axis.Z)
			return face != Direction.SOUTH;
		return super.reverseUVsVertically(state, face);
	}

	@Override
	protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
		if (large)
			return super.reverseUVsHorizontally(state, face);

		if (state.getValue(AXIS)
			.isVertical()
			&& face.getAxis()
				.isHorizontal())
			return true;

		if (state.getValue(AXIS) == Axis.Z && face == Direction.DOWN)
			return true;

		return super.reverseUVsHorizontally(state, face);
	}

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
		Axis axis = state.getValue(AXIS);
		if (large || axis == direction.getAxis()) {
			if (axis == direction.getAxis() && state
				.getValue(direction.getAxisDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT
					: EncasedCogwheelBlock.BOTTOM_SHAFT))
				return null;
			return super.getShift(state, direction, sprite);
		}
		return sideShifts.get(axis == Axis.X || axis == Axis.Z && direction.getAxis() == Axis.X);
	}

}
