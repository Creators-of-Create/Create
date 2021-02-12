package com.simibubi.create.foundation.render.backend.gl.shader;

import java.util.HashMap;
import java.util.Map;

public class ShaderUniforms {
    private final Map<String, GLSLType> uniforms;

    public ShaderUniforms() {
        this.uniforms = new HashMap<>();
    }
}
