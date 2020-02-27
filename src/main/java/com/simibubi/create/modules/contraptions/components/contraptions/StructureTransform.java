package com.simibubi.create.modules.contraptions.components.contraptions;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.AbstractChassisBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class StructureTransform {

	// Assuming structures cannot be rotated around multiple axes at once
	Rotation rotation;
	int angle;
	Axis rotationAxis;
	BlockPos offset;

	public StructureTransform(BlockPos offset, Vec3d rotation) {
		this.offset = offset;
		if (rotation.x != 0) {
			rotationAxis = Axis.X;
			angle = (int) (Math.round(rotation.x / 90) * 90);
		}
		if (rotation.y != 0) {
			rotationAxis = Axis.Y;
			angle = (int) (Math.round(rotation.y / 90) * 90);
		}
		if (rotation.z != 0) {
			rotationAxis = Axis.Z;
			angle = (int) (Math.round(rotation.z / 90) * 90);
		}

		angle %= 360;
		if (angle < -90)
			angle += 360;

		this.rotation = Rotation.NONE;
		if (angle == -90 || angle == 270)
			this.rotation = Rotation.CLOCKWISE_90;
		if (angle == 90)
			this.rotation = Rotation.COUNTERCLOCKWISE_90;
		if (angle == 180)
			this.rotation = Rotation.CLOCKWISE_180;

	}

	public BlockPos apply(BlockPos localPos) {
		Vec3d vec = VecHelper.getCenterOf(localPos);
		vec = VecHelper.rotateCentered(vec, angle, rotationAxis);
		localPos = new BlockPos(vec);
		return localPos.add(offset);
	}

	/**
	 * Minecraft does not support blockstate rotation around axes other than y. Add
	 * specific cases here for blockstates, that should react to rotations around
	 * horizontal axes
	 */
	public BlockState apply(BlockState state) {

		if (rotationAxis == Axis.Y)
			state = state.rotate(rotation);
		else {
			if (state.getBlock() instanceof AbstractChassisBlock)
				return rotateChassis(state);

			if (state.getBlock() instanceof StairsBlock) {
				if (state.get(StairsBlock.FACING).getAxis() != rotationAxis) {
					for (int i = 0; i < rotation.ordinal(); i++) {
						Direction direction = state.get(StairsBlock.FACING);
						Half half = state.get(StairsBlock.HALF);
						if (direction.getAxisDirection() == AxisDirection.POSITIVE ^ half == Half.BOTTOM
								^ direction.getAxis() == Axis.Z)
							state = state.cycle(StairsBlock.HALF);
						else
							state = state.with(StairsBlock.FACING, direction.getOpposite());
					}
				} else {
					if (rotation == Rotation.CLOCKWISE_180) {
						state = state.cycle(StairsBlock.HALF);
					}
				}
				return state;
			}

			if (state.has(BlockStateProperties.FACING)) {
				state = state.with(BlockStateProperties.FACING,
						transformFacing(state.get(BlockStateProperties.FACING)));

			} else if (state.has(BlockStateProperties.AXIS)) {
				state = state.with(BlockStateProperties.AXIS, transformAxis(state.get(BlockStateProperties.AXIS)));

			} else if (rotation == Rotation.CLOCKWISE_180) {
				state = state.rotate(rotation);
				if (state.has(SlabBlock.TYPE) && state.get(SlabBlock.TYPE) != SlabType.DOUBLE)
					state = state.with(SlabBlock.TYPE,
							state.get(SlabBlock.TYPE) == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM);
			}

		}

		return state;
	}

	protected Axis transformAxis(Axis axisIn) {
		Direction facing = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axisIn);
		facing = transformFacing(facing);
		Axis axis = facing.getAxis();
		return axis;
	}

	protected Direction transformFacing(Direction facing) {
		for (int i = 0; i < rotation.ordinal(); i++)
			facing = facing.rotateAround(rotationAxis);
		return facing;
	}

	private BlockState rotateChassis(BlockState state) {
		if (rotation == Rotation.NONE)
			return state;

		BlockState rotated = state.with(BlockStateProperties.AXIS, transformAxis(state.get(BlockStateProperties.AXIS)));
		AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();

		for (Direction face : Direction.values()) {
			BooleanProperty glueableSide = block.getGlueableSide(rotated, face);
			if (glueableSide != null)
				rotated = rotated.with(glueableSide, false);
		}

		for (Direction face : Direction.values()) {
			BooleanProperty glueableSide = block.getGlueableSide(state, face);
			if (glueableSide == null || !state.get(glueableSide))
				continue;
			Direction rotatedFacing = transformFacing(face);
			BooleanProperty rotatedGlueableSide = block.getGlueableSide(rotated, rotatedFacing);
			if (rotatedGlueableSide != null)
				rotated = rotated.with(rotatedGlueableSide, true);
		}

		return rotated;
	}

}
