package com.simibubi.create.foundation.utility;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Iterate {

	public static final boolean[] trueAndFalse = {true, false};
	public static final int[] zeroAndOne = {0, 1};
	public static final int[] positiveAndNegative = {1, -1};
	public static final Direction[] directions = Direction.values();
	public static final Direction[] horizontalDirections = getHorizontals();
	public static final Axis[] axes = Axis.values();
	public static final EnumSet<Direction.Axis> axisSet = EnumSet.allOf(Direction.Axis.class);

	private static Direction[] getHorizontals() {
		Direction[] directions = new Direction[4];
		for (int i = 0; i < 4; i++)
			directions[i] = Direction.byHorizontalIndex(i);
		return directions;
	}

	public static Direction[] directionsInAxis(Axis axis) {
		switch (axis) {
			case X:
				return new Direction[]{Direction.EAST, Direction.WEST};
			case Y:
				return new Direction[]{Direction.UP, Direction.DOWN};
			default:
			case Z:
				return new Direction[]{Direction.SOUTH, Direction.NORTH};
		}
	}

	public static List<BlockPos> hereAndBelow(BlockPos pos) {
		return Arrays.asList(pos, pos.down());
	}
	
	public static List<BlockPos> hereBelowAndAbove(BlockPos pos) {
		return Arrays.asList(pos, pos.down(), pos.up());
	}
}
