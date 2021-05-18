package com.jozufozu.flywheel.backend.gl.shader;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;

public interface ShaderSpecLoader<P extends GlProgram> {

	/**
	 * @param loader
	 * @param ctx
	 * @param spec
	 * @return
	 */
	IMultiProgram<P> create(ShaderLoader loader, ShaderContext<P> ctx, ProgramSpec spec);
}
