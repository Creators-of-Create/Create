package com.simibubi.create.foundation.render.effects;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.simibubi.create.foundation.render.AllProgramSpecs;

import net.minecraft.util.ResourceLocation;

public class EffectsContext extends ShaderContext<SphereFilterProgram> {

	public EffectsContext(Backend backend) {
		super(backend);
	}

	@Override
	public void load() {
		ProgramSpec programSpec = Backend.getInstance().getSpec(AllProgramSpecs.CHROMATIC);

		try {
			programs.put(programSpec.name, new SphereFilterProgram(loadAndLink(programSpec, null)));

			Backend.log.debug("Loaded program {}", programSpec.name);
		} catch (Exception e) {
			Backend.log.error("Program '{}': {}", programSpec.name, e);
			backend.sources.notifyError();
		}
	}

	@Override
	protected Shader getSource(ShaderType type, ResourceLocation name) {
		Shader source = super.getSource(type, name);
		source.processIncludes();
		return source;
	}
}
