package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.List;

import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.resources.ResourceLocation;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;

import net.minecraft.world.phys.AABB;
import com.mojang.math.Matrix4f;

public class ContraptionProgram extends WorldProgram {
	protected final int uLightBoxSize;
	protected final int uLightBoxMin;
	protected final int uModel;

	protected int uLightVolume;

	public ContraptionProgram(ResourceLocation name, int handle, List<IProgramExtension> extensions) {
		super(name, handle, extensions);
		uLightBoxSize = getUniformLocation("uLightBoxSize");
		uLightBoxMin = getUniformLocation("uLightBoxMin");
		uModel = getUniformLocation("uModel");
	}

	@Override
	protected void registerSamplers() {
		super.registerSamplers();
		uLightVolume = setSamplerBinding("uLightVolume", 4);
	}

    public void bind(Matrix4f model, AABB lightVolume) {
        double sizeX = lightVolume.maxX - lightVolume.minX;
        double sizeY = lightVolume.maxY - lightVolume.minY;
        double sizeZ = lightVolume.maxZ - lightVolume.minZ;
        GL20.glUniform3f(uLightBoxSize, (float) sizeX, (float) sizeY, (float) sizeZ);
        GL20.glUniform3f(uLightBoxMin, (float) lightVolume.minX, (float) lightVolume.minY, (float) lightVolume.minZ);
        uploadMatrixUniform(uModel, model);
    }
}
