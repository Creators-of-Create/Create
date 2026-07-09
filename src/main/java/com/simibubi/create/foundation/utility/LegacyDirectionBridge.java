package com.simibubi.create.foundation.utility;

import net.minecraft.core.Direction;

public class LegacyDirectionBridge {
	public static Direction nearest(double x, double y, double z, Direction fallback) {
		double ax = Math.abs(x);
		double ay = Math.abs(y);
		double az = Math.abs(z);
		if (ax == 0 && ay == 0 && az == 0)
			return fallback;
		if (ay >= ax && ay >= az)
			return y >= 0 ? Direction.UP : Direction.DOWN;
		if (az >= ax)
			return z >= 0 ? Direction.SOUTH : Direction.NORTH;
		return x >= 0 ? Direction.EAST : Direction.WEST;
	}
}
