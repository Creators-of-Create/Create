package com.jozufozu.flywheel.core.shader.extension;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.util.ResourceLocation;

public class UnitExtensionInstance implements IExtensionInstance {

	public static final ResourceLocation NAME = new ResourceLocation(Flywheel.ID, "unit");

	public UnitExtensionInstance(GlProgram program) { }

	@Override
	public void bind() {

	}

	@Override
	public ResourceLocation name() {
		return NAME;
	}
}
