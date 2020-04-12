package com.simibubi.create.foundation.utility;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class Iterate {

	public static final boolean[] trueAndFalse = { true, false };
	public static final int[] positiveAndNegative = { 1, -1 };
	public static final Direction[] directions = Direction.values();
	public static final Direction[] horizontalDirections = getHorizontals();
	public static final Axis[] axes = Axis.values();

	private static Direction[] getHorizontals() {
		Direction[] directions = new Direction[4];
		for (int i = 0; i < 4; i++)
			directions[i] = Direction.byHorizontalIndex(i);
		return directions;
	}
	
	public static List<BlockPos> hereAndBelow(BlockPos pos) {
		return Arrays.asList(pos, pos.down());
	}
	                                                    
	
}
