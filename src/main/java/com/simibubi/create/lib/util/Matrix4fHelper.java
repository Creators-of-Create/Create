package com.simibubi.create.lib.util;

import com.mojang.math.Matrix4f;
import com.simibubi.create.lib.extensions.Matrix4fExtensions;

public final class Matrix4fHelper {

	public static Matrix4f fromFloatArray(float[] values) {
		Matrix4f matrix = new Matrix4f();
		Matrix4fExtensions ext = get(matrix);

		ext.create$fromFloatArray(values);

		return matrix;
	}

	private static Matrix4fExtensions get(Matrix4f m) {
		return MixinHelper.cast(m);
	}

	private Matrix4fHelper() {}
}
