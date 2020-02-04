package com.simibubi.create.foundation.utility;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class AngleHelper {

	public static float horizontalAngle(Direction facing) {
		float angle = facing.getHorizontalAngle();
		if (facing.getAxis() == Axis.X)
			angle = -angle;
		return angle;
	}

	public static float verticalAngle(Direction facing) {
		return facing == Direction.UP ? -90 : facing == Direction.DOWN ? 90 : 0;
	}

	public static float rad(float angle) {
		return (float) (angle / 180 * Math.PI);
	}

}
