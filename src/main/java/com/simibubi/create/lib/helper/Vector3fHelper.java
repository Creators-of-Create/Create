package com.simibubi.create.lib.helper;

import com.mojang.math.Vector3f;
import com.simibubi.create.lib.mixin.accessor.Vector3fAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

public final class Vector3fHelper {
	public static void setX(Vector3f vector, float x) {
		get(vector).create$x(x);
	}

	public static void setY(Vector3f vector, float y) {
		get(vector).create$y(y);
	}

	public static void setZ(Vector3f vector, float z) {
		get(vector).create$z(z);
	}

	private static Vector3fAccessor get(Vector3f vector) {
		return MixinHelper.cast(vector);
	}

	private Vector3fHelper() {}
}
