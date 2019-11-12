package com.simibubi.create.modules.contraptions.relays.belt;

import static net.minecraft.block.Block.makeCuboidShape;

import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BeltShapes {

	private static final VoxelShape FULL = makeCuboidShape(0, 0, 0, 16, 16, 16),
			FLAT_STRAIGHT_X = makeCuboidShape(1, 3, 0, 15, 13, 16),
			FLAT_STRAIGHT_Z = makeCuboidShape(0, 3, 1, 16, 13, 15),
			VERTICAL_STRAIGHT_X = makeCuboidShape(3, 0, 1, 13, 16, 15),
			VERTICAL_STRAIGHT_Z = makeCuboidShape(1, 0, 3, 15, 16, 13),

			SLOPE_END_EAST = makeCuboidShape(0, 3, 1, 10, 13, 15),
			SLOPE_END_WEST = makeCuboidShape(6, 3, 1, 16, 13, 15),
			SLOPE_END_SOUTH = makeCuboidShape(1, 3, 0, 15, 13, 10),
			SLOPE_END_NORTH = makeCuboidShape(1, 3, 6, 15, 13, 16),

			SLOPE_BUILDING_BLOCK_X = makeCuboidShape(5, 5, 1, 11, 11, 15),
			SLOPE_BUILDING_BLOCK_Z = makeCuboidShape(1, 5, 5, 15, 11, 11),

			SLOPE_UPWARD_END_EAST = VoxelShapes.or(SLOPE_END_EAST, createHalfSlope(Direction.EAST, false)),
			SLOPE_UPWARD_END_WEST = VoxelShapes.or(SLOPE_END_WEST, createHalfSlope(Direction.WEST, false)),
			SLOPE_UPWARD_END_SOUTH = VoxelShapes.or(SLOPE_END_SOUTH, createHalfSlope(Direction.SOUTH, false)),
			SLOPE_UPWARD_END_NORTH = VoxelShapes.or(SLOPE_END_NORTH, createHalfSlope(Direction.NORTH, false)),

			SLOPE_DOWNWARD_END_EAST = VoxelShapes.or(SLOPE_END_EAST, createHalfSlope(Direction.EAST, true)),
			SLOPE_DOWNWARD_END_WEST = VoxelShapes.or(SLOPE_END_WEST, createHalfSlope(Direction.WEST, true)),
			SLOPE_DOWNWARD_END_SOUTH = VoxelShapes.or(SLOPE_END_SOUTH, createHalfSlope(Direction.SOUTH, true)),
			SLOPE_DOWNWARD_END_NORTH = VoxelShapes.or(SLOPE_END_NORTH, createHalfSlope(Direction.NORTH, true)),

			SLOPE_EAST = createSlope(Direction.EAST), SLOPE_WEST = createSlope(Direction.WEST),
			SLOPE_NORTH = createSlope(Direction.NORTH), SLOPE_SOUTH = createSlope(Direction.SOUTH);

	public static VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(BeltBlock.HORIZONTAL_FACING);
		Axis axis = facing.getAxis();
		Part part = state.get(BeltBlock.PART);
		Slope slope = state.get(BeltBlock.SLOPE);

		if (slope == Slope.HORIZONTAL)
			return axis == Axis.Z ? FLAT_STRAIGHT_X : FLAT_STRAIGHT_Z;
		if (slope == Slope.VERTICAL)
			return axis == Axis.X ? VERTICAL_STRAIGHT_X : VERTICAL_STRAIGHT_Z;

		if (part != Part.MIDDLE) {
			boolean upward = slope == Slope.UPWARD;
			if (part == Part.START)
				slope = upward ? Slope.DOWNWARD : Slope.UPWARD;
			else
				facing = facing.getOpposite();

			if (facing == Direction.NORTH)
				return upward ? SLOPE_UPWARD_END_NORTH : SLOPE_DOWNWARD_END_NORTH;
			if (facing == Direction.SOUTH)
				return upward ? SLOPE_UPWARD_END_SOUTH : SLOPE_DOWNWARD_END_SOUTH;
			if (facing == Direction.EAST)
				return upward ? SLOPE_UPWARD_END_EAST : SLOPE_DOWNWARD_END_EAST;
			if (facing == Direction.WEST)
				return upward ? SLOPE_UPWARD_END_WEST : SLOPE_DOWNWARD_END_WEST;
		}

		if (slope == Slope.DOWNWARD)
			facing = facing.getOpposite();

		if (facing == Direction.NORTH)
			return SLOPE_NORTH;
		if (facing == Direction.SOUTH)
			return SLOPE_SOUTH;
		if (facing == Direction.EAST)
			return SLOPE_EAST;
		if (facing == Direction.WEST)
			return SLOPE_WEST;

		return FULL;
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
