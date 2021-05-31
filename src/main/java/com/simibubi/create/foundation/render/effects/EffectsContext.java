package com.simibubi.create.foundation.render.effects;

import java.util.Collections;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.simibubi.create.foundation.render.AllProgramSpecs;

public class EffectsContext extends ShaderContext<SphereFilterProgram> {

	public static final EffectsContext INSTANCE = new EffectsContext();

	public EffectsContext() {
		super();
	}

	@Override
	protected IMultiProgram<SphereFilterProgram> loadSpecInternal(ShaderLoader loader, ProgramSpec spec) {
		return new SphereFilterProgram(loadProgram(loader, spec, Collections.emptyList()));
	}

	@Override
	public void load(ShaderLoader loader) {
		transformer = new ShaderTransformer()
				.pushStage(loader::processIncludes);
		loadProgramFromSpec(loader, Backend.getSpec(AllProgramSpecs.CHROMATIC));
	}
}
