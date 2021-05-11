package com.simibubi.create.foundation.render.effects;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.gl.shader.SingleProgram;
import com.simibubi.create.foundation.render.AllProgramSpecs;

import net.minecraft.util.ResourceLocation;

public class EffectsContext extends ShaderContext<SphereFilterProgram> {

	public static final EffectsContext INSTANCE = new EffectsContext();

	private final SingleProgram.SpecLoader<SphereFilterProgram> loader;

	public EffectsContext() {
		super(new ResourceLocation("create", "effects"));
		loader = new SingleProgram.SpecLoader<>(SphereFilterProgram::new);
	}

	@Override
	public void load(ShaderLoader loader) {
		loadProgramFromSpec(loader, AllProgramSpecs.CHROMATIC);
	}

	@Override
	public ShaderSpecLoader<SphereFilterProgram> getLoader() {
		return loader;
	}
}
