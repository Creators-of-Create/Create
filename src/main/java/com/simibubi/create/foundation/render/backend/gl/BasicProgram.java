package com.simibubi.create.foundation.render.backend.gl;

import org.lwjgl.opengl.GL20;

import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.gl.shader.GlProgram;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.ResourceLocation;

public class BasicProgram extends GlProgram {
    protected final int uTime;
    protected final int uViewProjection;
    protected final int uDebug;
    protected final int uCameraPos;
    protected final int uFogRange;
    protected final int uFogColor;

    protected int uBlockAtlas;
    protected int uLightMap;

    public BasicProgram(ResourceLocation name, int handle) {
        super(name, handle);
        uTime = getUniformLocation("uTime");
        uViewProjection = getUniformLocation("uViewProjection");
        uDebug = getUniformLocation("uDebug");
        uCameraPos = getUniformLocation("uCameraPos");
        uFogRange = getUniformLocation("uFogRange");
        uFogColor = getUniformLocation("uFogColor");

        bind();
        registerSamplers();
        unbind();
    }

    protected void registerSamplers() {
        uBlockAtlas = setSamplerBinding("uBlockAtlas", 0);
        uLightMap = setSamplerBinding("uLightMap", 2);
    }

    public void bind(Matrix4f viewProjection, double camX, double camY, double camZ, int debugMode) {
        super.bind();

        GL20.glUniform1i(uDebug, debugMode);
        GL20.glUniform1f(uTime, AnimationTickHolder.getRenderTick());

        uploadMatrixUniform(uViewProjection, viewProjection);
        GL20.glUniform3f(uCameraPos, (float) camX, (float) camY, (float) camZ);

        GL20.glUniform2f(uFogRange, GlFog.getFogStart(), GlFog.getFogEnd());
        GL20.glUniform4fv(uFogColor, GlFog.FOG_COLOR);
    }

    protected static void uploadMatrixUniform(int uniform, Matrix4f mat) {
        Backend.MATRIX_BUFFER.position(0);
        mat.write(Backend.MATRIX_BUFFER);
        Backend.MATRIX_BUFFER.rewind();
        GL20.glUniformMatrix4fv(uniform, false, Backend.MATRIX_BUFFER);
    }
}
