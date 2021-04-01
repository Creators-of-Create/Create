package com.simibubi.create.foundation.render.backend.gl.shader;

import org.lwjgl.opengl.GL20;

import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.gl.GlObject;
import com.simibubi.create.foundation.render.backend.gl.versioned.GlCompat;

import net.minecraft.util.ResourceLocation;

public class GlShader extends GlObject {

    public final ResourceLocation name;
    public final ShaderType type;

    public GlShader(ShaderType type, ResourceLocation name, String source) {
        this.type = type;
        this.name = name;
        int handle = GL20.glCreateShader(type.glEnum);

        GlCompat.safeShaderSource(handle, source);
        GL20.glCompileShader(handle);

        String log = GL20.glGetShaderInfoLog(handle);

        if (!log.isEmpty()) {
            Backend.log.error("Shader compilation log for " + name + ": " + log);
        }

        if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
            throw new RuntimeException("Could not compile shader. See log for details.");
        }

        setHandle(handle);
    }

    @Override
    protected void deleteInternal(int handle) {
        GL20.glDeleteShader(handle);
    }

    @FunctionalInterface
    public interface PreProcessor {
        String process(String source);

        default PreProcessor andThen(PreProcessor that) {
            return source -> that.process(this.process(source));
        }
    }
}
