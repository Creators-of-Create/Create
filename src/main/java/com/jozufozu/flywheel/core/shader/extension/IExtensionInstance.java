package com.jozufozu.flywheel.core.shader.extension;

import net.minecraft.util.ResourceLocation;

public interface IExtensionInstance {

	/**
	 * Bind the extra program state. It is recommended to grab the state information from global variables,
	 * or local variables passed through a {@link IProgramExtension}.
	 */
	void bind();

	ResourceLocation name();
}
