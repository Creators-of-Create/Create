package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.util.ResourceLocation;

public interface IShaderContext<P extends GlProgram> {

	P getProgram(ResourceLocation loc);

	/**
	 * Load all programs associated with this context. This might be just one, if the context is very specialized.
	 */
	void load();

	void delete();
}
