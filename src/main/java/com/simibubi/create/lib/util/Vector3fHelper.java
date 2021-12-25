package com.simibubi.create.lib.util;

import com.mojang.math.Vector3f;
import com.simibubi.create.lib.mixin.common.accessor.Vector3fAccessor;

public final class Vector3fHelper {
	public static void setX(Vector3f vector, float x) {
		get(vector).create$setX(x);
	}

	public static void setY(Vector3f vector, float y) {
		get(vector).create$setY(y);
	}

	public static void setZ(Vector3f vector, float z) {
		get(vector).create$setZ(z);
	}

	private static Vector3fAccessor get(Vector3f vector) {
		return MixinHelper.cast(vector);
	}

	private Vector3fHelper() {}
}
