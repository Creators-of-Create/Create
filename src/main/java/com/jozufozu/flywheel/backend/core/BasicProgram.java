package com.jozufozu.flywheel.backend.core;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramFogMode;
import com.jozufozu.flywheel.backend.loading.Program;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.math.vector.Matrix4f;

public class BasicProgram extends GlProgram {
	protected final int uTime;
	protected final int uViewProjection;
	protected final int uCameraPos;

	protected final ProgramFogMode fogMode;

	protected int uBlockAtlas;
	protected int uLightMap;

	public BasicProgram(Program program, ProgramFogMode.Factory fogFactory) {
		super(program);
		uTime = getUniformLocation("uTime");
		uViewProjection = getUniformLocation("uViewProjection");
		uCameraPos = getUniformLocation("uCameraPos");

		fogMode = fogFactory.create(this);

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

		fogMode.bind();
	}
}
