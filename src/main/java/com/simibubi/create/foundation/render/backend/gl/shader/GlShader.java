package com.simibubi.create.foundation.render.backend.gl.shader;

import com.simibubi.create.foundation.render.backend.gl.GlObject;
import com.simibubi.create.foundation.render.backend.Backend;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;

public class GlShader extends GlObject {

    public final ResourceLocation name;
    public final ShaderType type;

    public GlShader(ShaderType type, ResourceLocation name, String source, PreProcessor preProcessor) {
        this.type = type;
        this.name = name;
        int handle = GL20.glCreateShader(type.glEnum);

        if (preProcessor != null) {
            source = preProcessor.process(source);
            Backend.log.info("Preprocessor run on " + name);// + ":\n" + source);
        }

        GL20.glShaderSource(handle, source);
        GL20.glCompileShader(handle);

        String log = GL20.glGetShaderInfoLog(handle);

        if (!log.isEmpty()) {
            Backend.log.warn("Shader compilation log for " + name + ": " + log);
        }

        if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
            throw new RuntimeException("Could not compile shader");
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
