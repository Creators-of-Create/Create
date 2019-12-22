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
	
}
