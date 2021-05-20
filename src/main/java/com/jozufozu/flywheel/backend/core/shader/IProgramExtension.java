package com.jozufozu.flywheel.backend.core.shader;

import net.minecraft.util.ResourceLocation;

/**
 * A program extension to be passed to
 */
public interface IProgramExtension {

	/**
	 * Bind the extra program state. It is recommended to grab the state information from global variables,
	 * or local variables passed through a {@link ProgramExtender} closure.
	 */
	void bind();

	ResourceLocation name();
}
