package com.simibubi.create.foundation.render.backend.gl.shader;

import com.simibubi.create.foundation.render.backend.ShaderLoader;

public interface ShaderSpecLoader<P extends GlProgram> {
	IMultiProgram<P> create(ShaderLoader loader, ProgramSpec<P> spec);
}
