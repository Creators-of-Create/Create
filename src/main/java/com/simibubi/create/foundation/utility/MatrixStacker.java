package com.simibubi.create.foundation.utility;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public class MatrixStacker {

	static Vector3d center = VecHelper.getCenterOf(BlockPos.ZERO);
	static MatrixStacker instance;

	MatrixStack ms;

	public static MatrixStacker of(MatrixStack ms) {
		if (instance == null)
			instance = new MatrixStacker();
		instance.ms = ms;
		return instance;
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

	public MatrixStacker rotateRadians(double angleRoll, double angleYaw, double anglePitch) {
		rotateX(AngleHelper.deg(angleRoll));
		rotateY(AngleHelper.deg(angleYaw));
		rotateZ(AngleHelper.deg(anglePitch));
		return this;
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

	public MatrixStacker nudge(int id) {
		long randomBits = (long) id * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		ms.translate(xNudge, yNudge, zNudge);
		return this;
	}

	private MatrixStacker multiply(Vector3f axis, double angle) {
		if (angle == 0)
			return this;
		ms.multiply(axis.getDegreesQuaternion((float) angle));
		return this;
	}

}
