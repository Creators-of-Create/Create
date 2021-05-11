package com.jozufozu.flywheel.util;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RenderUtil {
	public static int nextPowerOf2(int a) {
		int h = Integer.highestOneBit(a);
		return (h == a) ? h : (h << 1);
	}

	public static boolean isPowerOf2(int n) {
		int b = n & (n - 1);
		return b == 0 && n != 0;
	}

	public static double lengthSqr(double x, double y, double z) {
		return x * x + y * y + z * z;
	}

	public static double length(double x, double y, double z) {
		return Math.sqrt(lengthSqr(x, y, z));
	}

	public static float[] writeMatrixStack(MatrixStack stack) {
		return writeMatrixStack(stack.peek().getModel(), stack.peek().getNormal());
	}

	// GPUs want matrices in column major order.
	public static float[] writeMatrixStack(Matrix4f model, Matrix3f normal) {
		return new float[]{
				model.a00,
				model.a10,
				model.a20,
				model.a30,
				model.a01,
				model.a11,
				model.a21,
				model.a31,
				model.a02,
				model.a12,
				model.a22,
				model.a32,
				model.a03,
				model.a13,
				model.a23,
				model.a33,
				normal.a00,
				normal.a10,
				normal.a20,
				normal.a01,
				normal.a11,
				normal.a21,
				normal.a02,
				normal.a12,
				normal.a22,
		};
	}

	public static float[] writeMatrix(Matrix4f model) {
		return new float[]{
				model.a00,
				model.a10,
				model.a20,
				model.a30,
				model.a01,
				model.a11,
				model.a21,
				model.a31,
				model.a02,
				model.a12,
				model.a22,
				model.a32,
				model.a03,
				model.a13,
				model.a23,
				model.a33,
		};
	}

	public static Supplier<MatrixStack> rotateToFace(Direction facing) {
		return () -> {
			MatrixStack stack = new MatrixStack();
//			MatrixStacker.of(stack)
//					.centre()
//					.rotateY(AngleHelper.horizontalAngle(facing))
//					.rotateX(AngleHelper.verticalAngle(facing))
//					.unCentre();
			stack.peek().getModel().setTranslation(0.5f, 0.5f, 0.5f);
			stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(AngleHelper.horizontalAngle(facing)));
			stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(AngleHelper.verticalAngle(facing)));
			stack.translate(-0.5f, -0.5f, -0.5f);
			return stack;
		};
	}
}
