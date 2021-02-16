package com.simibubi.create.foundation.utility;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class VecHelper {

	public static final Vec3d CENTER_OF_ORIGIN = new Vec3d(.5, .5, .5);

	public static Vec3d rotate(Vec3d vec, Vec3d rotationVec) {
		return rotate(vec, rotationVec.x, rotationVec.y, rotationVec.z);
	}

	public static Vec3d rotate(Vec3d vec, double xRot, double yRot, double zRot) {
		return rotate(rotate(rotate(vec, xRot, Axis.X), yRot, Axis.Y), zRot, Axis.Z);
	}

	public static Vec3d rotateCentered(Vec3d vec, double deg, Axis axis) {
		Vec3d shift = getCenterOf(BlockPos.ZERO);
		return VecHelper.rotate(vec.subtract(shift), deg, axis)
			.add(shift);
	}

	public static Vec3d rotate(Vec3d vec, double deg, Axis axis) {
		if (deg == 0)
			return vec;
		if (vec == Vec3d.ZERO)
			return vec;

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

	public static boolean isVecPointingTowards(Vec3d vec, Direction direction) {
		return new Vec3d(direction.getDirectionVec()).distanceTo(vec.normalize()) < .75;
	}

	public static Vec3d getCenterOf(Vec3i pos) {
		if (pos.equals(Vec3i.NULL_VECTOR))
			return CENTER_OF_ORIGIN;
		return new Vec3d(pos).add(.5f, .5f, .5f);
	}

	public static Vec3d offsetRandomly(Vec3d vec, Random r, float radius) {
		return new Vec3d(vec.x + (r.nextFloat() - .5f) * 2 * radius, vec.y + (r.nextFloat() - .5f) * 2 * radius,
			vec.z + (r.nextFloat() - .5f) * 2 * radius);
	}

	public static Vec3d axisAlingedPlaneOf(Vec3d vec) {
		vec = vec.normalize();
		return new Vec3d(1, 1, 1).subtract(Math.abs(vec.x), Math.abs(vec.y), Math.abs(vec.z));
	}

	public static Vec3d axisAlingedPlaneOf(Direction face) {
		return axisAlingedPlaneOf(new Vec3d(face.getDirectionVec()));
	}

	public static ListNBT writeNBT(Vec3d vec) {
		ListNBT listnbt = new ListNBT();
		listnbt.add(DoubleNBT.of(vec.x));
		listnbt.add(DoubleNBT.of(vec.y));
		listnbt.add(DoubleNBT.of(vec.z));
		return listnbt;
	}

	public static Vec3d readNBT(ListNBT list) {
		if (list.isEmpty())
			return Vec3d.ZERO;
		return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	public static Vec3d voxelSpace(double x, double y, double z) {
		return new Vec3d(x, y, z).scale(1 / 16f);
	}

	public static int getCoordinate(Vec3i pos, Axis axis) {
		return axis.getCoordinate(pos.getX(), pos.getY(), pos.getZ());
	}

	public static float getCoordinate(Vec3d vec, Axis axis) {
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

	public static Vec3d clamp(Vec3d vec, float maxLength) {
		return vec.length() > maxLength ? vec.normalize()
			.scale(maxLength) : vec;
	}

	public static Vec3d lerp(float p, Vec3d from, Vec3d to) {
		return from.add(from.subtract(to)
			.scale(p));
	}

	public static Vec3d clampComponentWise(Vec3d vec, float maxLength) {
		return new Vec3d(MathHelper.clamp(vec.x, -maxLength, maxLength), MathHelper.clamp(vec.y, -maxLength, maxLength),
			MathHelper.clamp(vec.z, -maxLength, maxLength));
	}

	public static Vec3d project(Vec3d vec, Vec3d ontoVec) {
		if (ontoVec.equals(Vec3d.ZERO))
			return Vec3d.ZERO;
		return ontoVec.scale(vec.dotProduct(ontoVec) / ontoVec.lengthSquared());
	}

	@Nullable
	public static Vec3d intersectSphere(Vec3d origin, Vec3d lineDirection, Vec3d sphereCenter, double radius) {
		if (lineDirection.equals(Vec3d.ZERO))
			return null;
		if (lineDirection.length() != 1)
			lineDirection = lineDirection.normalize();

		Vec3d diff = origin.subtract(sphereCenter);
		double lineDotDiff = lineDirection.dotProduct(diff);
		double delta = lineDotDiff * lineDotDiff - (diff.lengthSquared() - radius * radius);
		if (delta < 0)
			return null;
		double t = -lineDotDiff + MathHelper.sqrt(delta);
		return origin.add(lineDirection.scale(t));
	}

}
