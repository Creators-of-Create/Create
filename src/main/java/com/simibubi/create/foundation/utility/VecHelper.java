package com.simibubi.create.foundation.utility;

import java.util.Random;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class VecHelper {

	public static Vec3d rotate(Vec3d vec, float deg, Axis axis) {
		float angle = (float) (deg / 180f * Math.PI);

		double sin = MathHelper.sin(angle);
		double cos = MathHelper.cos(angle);
		double x = vec.x;
		double y = vec.y;
		double z = vec.z;

		if (axis == Axis.X)
			return new Vec3d(x, y * cos - z * sin, z * cos + y * sin);
		if (axis == Axis.Y)
			return new Vec3d(x * cos + z * sin, y, z * cos - x * sin);
		if (axis == Axis.Z)
			return new Vec3d(x * cos - y * sin, y * cos + x * sin, z);
		return vec;
	}

	public static Vec3d getCenterOf(Vec3i pos) {
		return new Vec3d(pos).add(.5f, .5f, .5f);
	}

	public static Vec3d offsetRandomly(Vec3d vec, Random r, float radius) {
		return new Vec3d(vec.x + (r.nextFloat() - .5f) * 2 * radius, vec.y + (r.nextFloat() - .5f) * 2 * radius,
				vec.z + (r.nextFloat() - .5f) * 2 * radius);
	}

}
