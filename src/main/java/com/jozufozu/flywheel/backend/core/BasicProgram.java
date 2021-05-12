package com.jozufozu.flywheel.backend.core;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramFogMode;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class BasicProgram extends GlProgram {
	protected final int uTime;
	protected final int uViewProjection;
	protected final int uDebug;
	protected final int uCameraPos;

	protected final ProgramFogMode fogMode;

	protected int uBlockAtlas;
	protected int uLightMap;

	public BasicProgram(GlProgram.Builder builder, ProgramFogMode.Factory fogFactory) {
		this(builder.name, builder.program, fogFactory);
	}

	public BasicProgram(ResourceLocation name, int handle, ProgramFogMode.Factory fogFactory) {
		super(name, handle);
		uTime = getUniformLocation("uTime");
		uViewProjection = getUniformLocation("uViewProjection");
		uDebug = getUniformLocation("uDebug");
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

	public void bind(Matrix4f viewProjection, double camX, double camY, double camZ, int debugMode) {
		super.bind();

		glUniform1i(uDebug, debugMode);
		glUniform1f(uTime, AnimationTickHolder.getRenderTime());

		uploadMatrixUniform(uViewProjection, viewProjection);
		glUniform3f(uCameraPos, (float) camX, (float) camY, (float) camZ);

		fogMode.bind();
	}
}
