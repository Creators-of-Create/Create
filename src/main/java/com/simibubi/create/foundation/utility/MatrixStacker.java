package com.simibubi.create.foundation.utility;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public class MatrixStacker {

	public static final Vector3d center = VecHelper.getCenterOf(BlockPos.ZERO);
	static MatrixStacker instance;

	MatrixStack ms;

	public static MatrixStacker of(MatrixStack ms) {
		if (instance == null)
			instance = new MatrixStacker();
		instance.ms = ms;
		return instance;
	}

	public MatrixStacker restoreIdentity() {
		MatrixStack.Entry entry = ms.peek();

		entry.getModel()
			.loadIdentity();
		entry.getNormal()
			.loadIdentity();

		return this;
	}

	public MatrixStacker rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		ms.multiply(axis.getUnitVector()
			.getRadialQuaternion(radians));
		return this;
	}

	public MatrixStacker rotate(double angle, Axis axis) {
		Vector3f vec =
			axis == Axis.X ? Vector3f.POSITIVE_X : axis == Axis.Y ? Vector3f.POSITIVE_Y : Vector3f.POSITIVE_Z;
		return multiply(vec, angle);
	}

	public MatrixStacker rotateX(double angle) {
		return multiply(Vector3f.POSITIVE_X, angle);
	}

	public MatrixStacker rotateY(double angle) {
		return multiply(Vector3f.POSITIVE_Y, angle);
	}

	public MatrixStacker rotateZ(double angle) {
		return multiply(Vector3f.POSITIVE_Z, angle);
	}

	public MatrixStacker centre() {
		return translate(center);
	}

	public MatrixStacker unCentre() {
		return translateBack(center);
	}

	public MatrixStacker translate(Vector3i vec) {
		ms.translate(vec.getX(), vec.getY(), vec.getZ());
		return this;
	}

	public MatrixStacker translate(Vector3d vec) {
		ms.translate(vec.x, vec.y, vec.z);
		return this;
	}

	public MatrixStacker translateBack(Vector3d vec) {
		ms.translate(-vec.x, -vec.y, -vec.z);
		return this;
	}

	public MatrixStacker translate(double x, double y, double z) {
		ms.translate(x, y, z);
		return this;
	}

	public MatrixStacker multiply(Quaternion quaternion) {
		ms.multiply(quaternion);
		return this;
	}

	public MatrixStacker nudge(int id) {
		long randomBits = (long) id * 31L * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		ms.translate(xNudge, yNudge, zNudge);
		return this;
	}

	public MatrixStacker multiply(Vector3f axis, double angle) {
		if (angle == 0)
			return this;
		ms.multiply(axis.getDegreesQuaternion((float) angle));
		return this;
	}

	public MatrixStacker push() {
		ms.push();
		return this;
	}

	public MatrixStacker pop() {
		ms.pop();
		return this;
	}

	public MatrixStack unwrap() {
		return ms;
	}

}
