package com.jozufozu.flywheel.backend.core;

import static org.lwjgl.opengl.GL20.glUniform2f;

import java.util.List;

import com.jozufozu.flywheel.backend.core.shader.WorldProgram;
import com.jozufozu.flywheel.backend.core.shader.extension.IProgramExtension;
import com.jozufozu.flywheel.backend.loading.Program;

public class CrumblingProgram extends WorldProgram {
	protected final int uTextureScale;
	protected int uCrumbling;

	public CrumblingProgram(Program program, List<IProgramExtension> extensions) {
		super(program, extensions);

		uTextureScale = getUniformLocation("uTextureScale");
	}

	@Override
	protected void registerSamplers() {
		super.registerSamplers();
		uCrumbling = setSamplerBinding("uCrumbling", 4);
	}

	public void setTextureScale(float x, float y) {
		glUniform2f(uTextureScale, x, y);
	}

}
