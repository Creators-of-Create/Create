package com.simibubi.create.foundation.render.backend.gl;

import org.lwjgl.opengl.GL20;

public class GlFog {
    public static float getFogEnd() {
        return GL20.glGetFloat(GL20.GL_FOG_END);
    }

    public static float getFogStart() {
        return GL20.glGetFloat(GL20.GL_FOG_START);
    }
}
