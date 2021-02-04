package com.simibubi.create.foundation.render.gl;

import org.lwjgl.opengl.GL20;

public enum SamplerType {
    SAMPLER2D(GL20.GL_TEXTURE_2D, "sampler2D"),
    SAMPLER3D(GL20.GL_TEXTURE_3D, "sampler3D"),
    ;

    private final int glEnum;
    private final String shaderToken;

    SamplerType(int glEnum, String shaderToken) {
        this.glEnum = glEnum;
        this.shaderToken = shaderToken;
    }

    public int getGlEnum() {
        return glEnum;
    }

    public String getShaderToken() {
        return shaderToken;
    }
}
