package com.simibubi.create.foundation.render.contraption;

import com.simibubi.create.foundation.render.gl.BasicProgram;
import com.simibubi.create.foundation.render.light.GridAlignedBB;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;

public class ContraptionProgram extends BasicProgram {
    protected final int uLightBoxSize;
    protected final int uLightBoxMin;
    protected final int uModel;

    protected int uLightVolume;

    public ContraptionProgram(ResourceLocation name, int handle) {
        super(name, handle);
        uLightBoxSize = getUniformLocation("uLightBoxSize");
        uLightBoxMin = getUniformLocation("uLightBoxMin");
        uModel = getUniformLocation("uModel");
    }

    @Override
    protected void registerSamplers() {
        super.registerSamplers();
        uLightVolume = setSamplerBinding("uLightVolume", 4);
    }

    public void bind(Matrix4f model, GridAlignedBB lightVolume) {
        bind();
        GL20.glUniform3f(uLightBoxSize, lightVolume.sizeX(), lightVolume.sizeY(), lightVolume.sizeZ());
        GL20.glUniform3f(uLightBoxMin, lightVolume.minX, lightVolume.minY, lightVolume.minZ);
        uploadMatrixUniform(uModel, model);
    }
}
