package com.jozufozu.flywheel.backend.core.shader;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

public enum GlFogMode implements ProgramExtender {
	NONE(FogMode.None::new),
	LINEAR(FogMode.Linear::new, "USE_FOG_LINEAR"),
	EXP2(FogMode.Exp2::new, "USE_FOG_EXP2"),
	;

	public static final String USE_FOG = "USE_FOG";

	private final ProgramExtender fogFactory;
	private final List<String> defines;

	GlFogMode(ProgramExtender fogFactory) {
		this.fogFactory = fogFactory;
		this.defines = Collections.emptyList();
	}

	GlFogMode(ProgramExtender fogFactory, String name) {
		this.fogFactory = fogFactory;
		this.defines = Lists.newArrayList(USE_FOG, name);
	}

	public List<String> getDefines() {
		return defines;
	}

	@Override
	public IProgramExtension create(GlProgram program) {
		return fogFactory.create(program);
	}
}
