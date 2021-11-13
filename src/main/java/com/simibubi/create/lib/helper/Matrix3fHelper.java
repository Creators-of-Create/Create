package com.simibubi.create.lib.helper;

import com.mojang.math.Matrix3f;
import com.simibubi.create.lib.extensions.Matrix3fExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

public final class Matrix3fHelper {

	public static float[] writeMatrix(Matrix3f matrix) {
		return get(matrix).create$writeMatrix();
	}

	private static Matrix3fExtensions get(Matrix3f m) {
		return MixinHelper.cast(m);
	}

	private Matrix3fHelper() {}
}
