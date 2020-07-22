package com.simibubi.create.foundation.utility;

import java.util.Random;

import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class VecHelper {

	public static Vector3d rotate(Vector3d vec, Vector3d rotationVec) {
		return rotate(vec, rotationVec.x, rotationVec.y, rotationVec.z);
	}
	
	public static Vector3d rotate(Vector3d vec, double xRot, double yRot, double zRot) {
		return rotate(rotate(rotate(vec, xRot, Axis.X), yRot, Axis.Y), zRot, Axis.Z);
	}

	public static Vector3d rotateCentered(Vector3d vec, double deg, Axis axis) {
		Vector3d shift = getCenterOf(BlockPos.ZERO);
		return VecHelper.rotate(vec.subtract(shift), deg, axis)
			.add(shift);
	}

	public static Vector3d rotate(Vector3d vec, double deg, Axis axis) {
		if (deg == 0)
			return vec;
		if (vec == Vector3d.ZERO)
			return vec;

		float angle = (float) (deg / 180f * Math.PI);
		double sin = MathHelper.sin(angle);
		double cos = MathHelper.cos(angle);
		double x = vec.x;
		double y = vec.y;
		double z = vec.z;

		if (axis == Axis.X)
			return new Vector3d(x, y * cos - z * sin, z * cos + y * sin);
		if (axis == Axis.Y)
			return new Vector3d(x * cos + z * sin, y, z * cos - x * sin);
		if (axis == Axis.Z)
			return new Vector3d(x * cos - y * sin, y * cos + x * sin, z);
		return vec;
	}

	public static boolean isVecPointingTowards(Vector3d vec, Direction direction) {
		return Vector3d.of(direction.getDirectionVec()).distanceTo(vec.normalize()) < .75;
	}

	public static Vector3d getCenterOf(Vector3i pos) {
		return Vector3d.of(pos).add(.5f, .5f, .5f);
	}

	public static Vector3d offsetRandomly(Vector3d vec, Random r, float radius) {
		return new Vector3d(vec.x + (r.nextFloat() - .5f) * 2 * radius, vec.y + (r.nextFloat() - .5f) * 2 * radius,
			vec.z + (r.nextFloat() - .5f) * 2 * radius);
	}

	public static Vector3d planeByNormal(Vector3d vec) {
		vec = vec.normalize();
		return new Vector3d(1, 1, 1).subtract(Math.abs(vec.x), Math.abs(vec.y), Math.abs(vec.z));
	}

	public static ListNBT writeNBT(Vector3d vec) {
		ListNBT listnbt = new ListNBT();
		listnbt.add(DoubleNBT.of(vec.x));
		listnbt.add(DoubleNBT.of(vec.y));
		listnbt.add(DoubleNBT.of(vec.z));
		return listnbt;
	}

	public static Vector3d readNBT(ListNBT list) {
		return new Vector3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	public static Vector3d voxelSpace(double x, double y, double z) {
		return new Vector3d(x, y, z).scale(1 / 16f);
	}

	public static int getCoordinate(Vector3i pos, Axis axis) {
		return axis.getCoordinate(pos.getX(), pos.getY(), pos.getZ());
	}

	public static float getCoordinate(Vector3d vec, Axis axis) {
		return (float) axis.getCoordinate(vec.x, vec.y, vec.z);
	}

	public static boolean onSameAxis(BlockPos pos1, BlockPos pos2, Axis axis) {
		if (pos1.equals(pos2))
			return true;
		for (Axis otherAxis : Axis.values())
			if (axis != otherAxis)
				if (getCoordinate(pos1, otherAxis) != getCoordinate(pos2, otherAxis))
					return false;
		return true;
	}

}
