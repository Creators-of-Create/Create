package com.jozufozu.flywheel.backend.gl.shader;

import com.jozufozu.flywheel.backend.ShaderLoader;

public interface ShaderSpecLoader<P extends GlProgram> {
	IMultiProgram<P> create(ShaderLoader loader, ProgramSpec<P> spec);
}
