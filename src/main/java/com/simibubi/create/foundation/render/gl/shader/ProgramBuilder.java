package com.simibubi.create.foundation.render.gl.shader;

import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public class ProgramBuilder {

    private final ResourceLocation name;
    private final Map<ShaderType, ResourceLocation> shaders;

    private ShaderConstants constants;

    public ProgramBuilder(ResourceLocation name) {
        this.name = name;
        shaders = new EnumMap<>(ShaderType.class);
    }

    public ResourceLocation getName() {
        return name;
    }

    public Map<ShaderType, ResourceLocation> getShaders() {
        return shaders;
    }

    public ShaderConstants getConstants() {
        return constants;
    }

    public ProgramBuilder setConstants(ShaderConstants constants) {
        this.constants = constants;
        return this;
    }

    public ProgramBuilder vert(ResourceLocation file) {
        return shader(ShaderType.VERTEX, file);
    }

    public ProgramBuilder frag(ResourceLocation file) {
        return shader(ShaderType.FRAGMENT, file);
    }

    public ProgramBuilder shader(ShaderType type, ResourceLocation file) {
        shaders.put(type, file);
        return this;
    }
}
