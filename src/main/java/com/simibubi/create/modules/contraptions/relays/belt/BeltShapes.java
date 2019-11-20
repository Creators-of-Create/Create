package com.simibubi.create.modules.contraptions.relays.belt;

import static net.minecraft.block.Block.makeCuboidShape;

import com.simibubi.create.foundation.utility.VoxelShaper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class BeltShapes {

	private static final VoxelShape SLOPE_BUILDING_BLOCK_X = makeCuboidShape(5, 5, 1, 11, 11, 15),
			SLOPE_BUILDING_BLOCK_Z = makeCuboidShape(1, 5, 5, 15, 11, 11),
			CASING_HORIZONTAL = makeCuboidShape(0, 0, 0, 16, 11, 16);

	private static final VoxelShaper SLOPE_END = VoxelShaper.forHorizontal(makeCuboidShape(1, 3, 0, 15, 13, 10)),
			SLOPE_TOP_END = VoxelShaper.forHorizontal(
					VoxelShapes.or(SLOPE_END.get(Direction.SOUTH), createHalfSlope(Direction.SOUTH, false))),
			SLOPE_BOTTOM_END = VoxelShaper.forHorizontal(
					VoxelShapes.or(SLOPE_END.get(Direction.SOUTH), createHalfSlope(Direction.SOUTH, true))),
			FLAT_STRAIGHT = VoxelShaper.forHorizontalAxis(makeCuboidShape(0, 3, 1, 16, 13, 15)),
			VERTICAL_STRAIGHT = VoxelShaper.forHorizontalAxis(makeCuboidShape(1, 0, 3, 15, 16, 13)),
			SLOPE_STRAIGHT = VoxelShaper.forHorizontal(createSlope(Direction.SOUTH)),
			CASING_TOP_END = VoxelShaper.forHorizontal(makeCuboidShape(0, 0, 0, 16, 11, 11));

	public static VoxelShape getShape(BlockState state) {
		Direction facing = state.get(BeltBlock.HORIZONTAL_FACING);
		Axis axis = facing.getAxis();
		Axis perpendicularAxis = facing.rotateY().getAxis();
		Part part = state.get(BeltBlock.PART);
		Slope slope = state.get(BeltBlock.SLOPE);

		if (slope == Slope.HORIZONTAL)
			return FLAT_STRAIGHT.get(perpendicularAxis);
		if (slope == Slope.VERTICAL)
			return VERTICAL_STRAIGHT.get(axis);

		if (part != Part.MIDDLE && part != Part.PULLEY) {
			boolean upward = slope == Slope.UPWARD;
			if (part == Part.START)
				upward = !upward;
			else
				facing = facing.getOpposite();

			return upward ? SLOPE_TOP_END.get(facing) : SLOPE_BOTTOM_END.get(facing);
		}

		if (slope == Slope.DOWNWARD)
			facing = facing.getOpposite();

		return SLOPE_STRAIGHT.get(facing);
	}

	public static VoxelShape getCasingShape(BlockState state) {
		if (!state.get(BeltBlock.CASING))
			return VoxelShapes.empty();

		Direction facing = state.get(BeltBlock.HORIZONTAL_FACING);
		Part part = state.get(BeltBlock.PART);
		Slope slope = state.get(BeltBlock.SLOPE);

		if (slope == Slope.HORIZONTAL)
			return CASING_HORIZONTAL;
		if (slope == Slope.VERTICAL)
			return VoxelShapes.empty();

		if (part != Part.MIDDLE) {
			boolean upward = slope == Slope.UPWARD;
			if (part == Part.START)
				upward = !upward;
			else
				facing = facing.getOpposite();

			return upward ? CASING_TOP_END.get(facing) : CASING_HORIZONTAL;
		}

		if (slope == Slope.DOWNWARD)
			facing = facing.getOpposite();

		return CASING_TOP_END.get(facing.getOpposite());
	}

	protected static VoxelShape createSlope(Direction facing) {
		return VoxelShapes.or(createHalfSlope(facing.getOpposite(), false), createHalfSlope(facing, true));
	}

	protected static VoxelShape createHalfSlope(Direction facing, boolean upward) {
		VoxelShape shape = VoxelShapes.empty();
		VoxelShape buildingBlock = facing.getAxis() == Axis.X ? SLOPE_BUILDING_BLOCK_X : SLOPE_BUILDING_BLOCK_Z;
		Vec3i directionVec = facing.getDirectionVec();

		int x = directionVec.getX();
		int y = upward ? 1 : -1;
		int z = directionVec.getZ();

		for (int segment = 0; segment < 6; segment++)
			shape = VoxelShapes.or(shape,
					buildingBlock.withOffset(x * segment / 16f, y * segment / 16f, z * segment / 16f));

		if (!upward)
			return shape;

		VoxelShape mask = makeCuboidShape(0, -8, 0, 16, 24, 16);
		for (int segment = 6; segment < 11; segment++)
			shape = VoxelShapes.or(shape,
					VoxelShapes.combine(mask,
							buildingBlock.withOffset(x * segment / 16f, y * segment / 16f, z * segment / 16f),
							IBooleanFunction.AND));
		return shape;
	}

}
