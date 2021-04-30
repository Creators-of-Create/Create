package com.simibubi.create.foundation.render.backend.gl;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.simibubi.create.foundation.render.backend.gl.shader.ProgramFogMode;

public enum GlFogMode {
	NONE(ProgramFogMode.None::new),
	LINEAR(ProgramFogMode.Linear::new, "USE_FOG_LINEAR"),
	EXP2(ProgramFogMode.Exp2::new, "USE_FOG_EXP2"),
	;

	public static final String USE_FOG = "USE_FOG";

	private final ProgramFogMode.Factory fogFactory;
	private final List<String> defines;

	GlFogMode(ProgramFogMode.Factory fogFactory) {
		this.fogFactory = fogFactory;
		this.defines = Collections.emptyList();
	}

	GlFogMode(ProgramFogMode.Factory fogFactory, String name) {
		this.fogFactory = fogFactory;
		this.defines = Lists.newArrayList(USE_FOG, name);
	}

	public List<String> getDefines() {
		return defines;
	}

	public ProgramFogMode.Factory getFogFactory() {
		return fogFactory;
	}
}
