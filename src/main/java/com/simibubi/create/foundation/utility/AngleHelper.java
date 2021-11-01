package com.simibubi.create.foundation.utility;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class AngleHelper {

	/**
	 * Legacy method. See {@link #horizontalAngleNew(Direction)} for new method.
	 */
	public static float horizontalAngle(Direction facing) {
		float angle = facing.toYRot();
		if (facing.getAxis() == Axis.X)
			angle = -angle;
		return angle;
	}

	/**
	 * Same as {@link #horizontalAngle(Direction)}, but returns 0 instead of -90 for vertical directions.
	 */
	public static float horizontalAngleNew(Direction facing) {
		if (facing.getAxis().isVertical()) {
			return 0;
		}
		float angle = facing.toYRot();
		if (facing.getAxis() == Axis.X)
			angle = -angle;
		return angle;
	}

	public static float verticalAngle(Direction facing) {
		return facing == Direction.UP ? -90 : facing == Direction.DOWN ? 90 : 0;
	}

	public static float rad(double angle) {
		if (angle == 0)
			return 0;
		return (float) (angle / 180 * Math.PI);
	}

	public static float deg(double angle) {
		if (angle == 0)
			return 0;
		return (float) (angle * 180 / Math.PI);
	}

	public static float angleLerp(double pct, double current, double target) {
		return (float) (current + getShortestAngleDiff(current, target) * pct);
	}

	public static float getShortestAngleDiff(double current, double target) {
		current = current % 360;
		target = target % 360;
		return (float) (((((target - current) % 360) + 540) % 360) - 180);
	}

}
