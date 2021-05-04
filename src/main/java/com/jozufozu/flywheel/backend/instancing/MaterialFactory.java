package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.core.BasicProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;

public class MaterialFactory {

	ProgramSpec programSpec;
	ModelFactory<?> modelFactory;

	public MaterialFactory(ProgramSpec programSpec, ModelFactory<?> modelFactory) {
		this.programSpec = programSpec;
		this.modelFactory = modelFactory;
	}

	public <P extends BasicProgram> RenderMaterial<P, ?> create(InstancedTileRenderer<P> renderer) {
		return new RenderMaterial<>(renderer, programSpec, modelFactory);
	}
}
