package com.simibubi.create.foundation.render.gl;

import com.simibubi.create.foundation.render.gl.backend.Backend;
import com.simibubi.create.foundation.render.gl.shader.GlProgram;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;

public class BasicProgram extends GlProgram {
    protected final int uTicks;
    protected final int uTime;
    protected final int uViewProjection;
    protected final int uDebug;

    protected int uBlockAtlas;
    protected int uLightMap;

    public BasicProgram(ResourceLocation name, int handle) {
        super(name, handle);
        uTicks = getUniformLocation("uTicks");
        uTime = getUniformLocation("uTime");
        uViewProjection = getUniformLocation("uViewProjection");
        uDebug = getUniformLocation("uDebug");

        bind();
        registerSamplers();
        unbind();
    }

    protected void registerSamplers() {
        uBlockAtlas = setSamplerBinding("uBlockAtlas", 0);
        uLightMap = setSamplerBinding("uLightMap", 2);
    }

    public void bind(Matrix4f viewProjection, int debugMode) {
        super.bind();

        GL20.glUniform1i(uTicks, AnimationTickHolder.getTicks());
        GL20.glUniform1f(uTime, AnimationTickHolder.getRenderTick());
        uploadMatrixUniform(uViewProjection, viewProjection);
        GL20.glUniform1i(uDebug, debugMode);
    }

    protected static void uploadMatrixUniform(int uniform, Matrix4f mat) {
        Backend.MATRIX_BUFFER.position(0);
        mat.write(Backend.MATRIX_BUFFER);
        Backend.MATRIX_BUFFER.rewind();
        GL20.glUniformMatrix4fv(uniform, false, Backend.MATRIX_BUFFER);
    }
}
