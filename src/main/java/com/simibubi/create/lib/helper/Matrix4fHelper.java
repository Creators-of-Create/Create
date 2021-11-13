package com.simibubi.create.lib.helper;

import org.jetbrains.annotations.Contract;

import com.mojang.math.Matrix4f;
import com.simibubi.create.lib.extensions.Matrix4fExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

public final class Matrix4fHelper {
	@Contract(mutates = "param1")
	public static void multiplyBackward(Matrix4f $this, Matrix4f other) {
		Matrix4f copy = other.copy();
		copy.multiply($this); // Uno reverse card
		get($this).create$set(copy);
	}

	public static Matrix4f fromFloatArray(float[] values) {
		Matrix4f matrix = new Matrix4f();
		Matrix4fExtensions ext = get(matrix);

		ext.create$fromFloatArray(values);

		return matrix;
	}

	public static float[] writeMatrix(Matrix4f matrix) {
		return get(matrix).create$writeMatrix();
	}

	private static Matrix4fExtensions get(Matrix4f m) {
		return MixinHelper.cast(m);
	}

	private Matrix4fHelper() {}
}
