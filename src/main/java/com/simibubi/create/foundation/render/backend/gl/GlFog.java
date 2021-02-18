package com.simibubi.create.foundation.render.backend.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class GlFog {
    public static float[] FOG_COLOR = new float[] {0, 0, 0, 0};

    public static boolean fogEnabled() {
        return GlStateManager.FOG.field_179049_a.field_179201_b;
    }

    public static int getFogMode() {
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
}
