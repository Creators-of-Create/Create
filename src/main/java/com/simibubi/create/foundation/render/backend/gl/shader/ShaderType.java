package com.simibubi.create.foundation.render.backend.gl.shader;

import org.lwjgl.opengl.GL20;

public enum ShaderType {
    VERTEX(GL20.GL_VERTEX_SHADER),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER),
    ;

    public final int glEnum;

    ShaderType(int glEnum) {
        this.glEnum = glEnum;
    }
}
