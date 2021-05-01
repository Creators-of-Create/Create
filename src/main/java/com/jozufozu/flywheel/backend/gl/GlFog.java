package com.jozufozu.flywheel.backend.gl;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

public class GlFog {
	public static float[] FOG_COLOR = new float[]{0, 0, 0, 0};

	public static boolean fogEnabled() {
		return GlStateManager.FOG.field_179049_a.field_179201_b;
	}

	public static int getFogModeGlEnum() {
		return GlStateManager.FOG.field_179047_b;
	}

	public static float getFogDensity() {
		return GlStateManager.FOG.field_179048_c;
	}

	public static float getFogEnd() {
		return GlStateManager.FOG.field_179046_e;
	}

	public static float getFogStart() {
		return GlStateManager.FOG.field_179045_d;
	}

	public static GlFogMode getFogMode() {
		if (!fogEnabled()) {
			return GlFogMode.NONE;
		}

		int mode = getFogModeGlEnum();

		switch (mode) {
			case GL11.GL_EXP2:
			case GL11.GL_EXP:
				return GlFogMode.EXP2;
			case GL11.GL_LINEAR:
				return GlFogMode.LINEAR;
			default:
				throw new UnsupportedOperationException("Unknown fog mode: " + mode);
		}
	}
}
