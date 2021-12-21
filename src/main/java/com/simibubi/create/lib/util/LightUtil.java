package com.simibubi.create.lib.util;

public class LightUtil {
	public static float diffuseLight(float x, float y, float z) {
		return Math.min(x * x * 0.6f + y * y * ((3f + y) / 4f) + z * z * 0.8f, 1f);
	}

	public static int getLightOffset(int v) {
		return (v * 8) + 6;
	}

	private LightUtil() {}
}
