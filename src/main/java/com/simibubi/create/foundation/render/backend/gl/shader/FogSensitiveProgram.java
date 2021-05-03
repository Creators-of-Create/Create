package com.simibubi.create.foundation.render.backend.gl.shader;

import java.util.Map;

import com.simibubi.create.foundation.render.backend.gl.GlFog;
import com.simibubi.create.foundation.render.backend.gl.GlFogMode;

public class FogSensitiveProgram<P extends GlProgram> implements IMultiProgram<P> {

	private final Map<GlFogMode, P> programs;

	public FogSensitiveProgram(Map<GlFogMode, P> programs) {
		this.programs = programs;
	}

	@Override
	public P get() {
		return programs.get(GlFog.getFogMode());
	}

	@Override
	public void delete() {
		programs.values().forEach(GlProgram::delete);
	}

}
