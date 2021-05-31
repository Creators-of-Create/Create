package com.jozufozu.flywheel.core.shader;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;

import java.util.List;

import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.math.vector.Matrix4f;

public class WorldProgram extends ExtensibleGlProgram {
	protected final int uTime;
	protected final int uViewProjection;
	protected final int uCameraPos;

	protected int uBlockAtlas;
	protected int uLightMap;

	public WorldProgram(Program program, List<IProgramExtension> extensions) {
		super(program, extensions);
		uTime = getUniformLocation("uTime");
		uViewProjection = getUniformLocation("uViewProjection");
		uCameraPos = getUniformLocation("uCameraPos");

		bind();
		registerSamplers();
		unbind();
	}

	protected void registerSamplers() {
		uBlockAtlas = setSamplerBinding("uBlockAtlas", 0);
		uLightMap = setSamplerBinding("uLightMap", 2);
	}

	public void uploadViewProjection(Matrix4f viewProjection) {
		uploadMatrixUniform(uViewProjection, viewProjection);
	}

	public void uploadCameraPos(double camX, double camY, double camZ) {
		glUniform3f(uCameraPos, (float) camX, (float) camY, (float) camZ);
	}

	public void uploadTime(float renderTime) {
		glUniform1f(uTime, renderTime);
	}

	@Override
	public void bind() {
		super.bind();

		uploadTime(AnimationTickHolder.getRenderTime());
	}
}
