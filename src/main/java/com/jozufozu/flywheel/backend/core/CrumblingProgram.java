package com.jozufozu.flywheel.backend.core;

import static org.lwjgl.opengl.GL20.glUniform2f;

import com.jozufozu.flywheel.backend.gl.shader.ProgramFogMode;

import net.minecraft.util.ResourceLocation;

public class CrumblingProgram extends BasicProgram {
	protected final int uTextureScale;

	public CrumblingProgram(ResourceLocation name, int handle, ProgramFogMode.Factory fogFactory) {
		super(name, handle, fogFactory);

		uTextureScale = getUniformLocation("uTextureScale");
	}

	public void setTextureScale(float x, float y) {
		glUniform2f(uTextureScale, x, y);
	}

}
