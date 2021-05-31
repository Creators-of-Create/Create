package com.jozufozu.flywheel.util;

import org.lwjgl.opengl.GL20;

public class AttribUtil {

	public static void enableArrays(int count) {
		enableArrays(0, count);
	}

	public static void enableArrays(int fromInclusive, int toExclusive) {
		for (int i = fromInclusive; i < toExclusive; i++) {
			GL20.glEnableVertexAttribArray(i);
		}
	}

	public static void disableArrays(int count) {
		disableArrays(0, count);
	}

	public static void disableArrays(int fromInclusive, int toExclusive) {
		for (int i = fromInclusive; i < toExclusive; i++) {
			GL20.glDisableVertexAttribArray(i);
		}
	}
}
