package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.core.BasicProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;

public class MaterialSpec {

	private final ProgramSpec programSpec;
	private final ModelFactory<?> modelFactory;

	public MaterialSpec(ProgramSpec programSpec, ModelFactory<?> modelFactory) {
		this.programSpec = programSpec;
		this.modelFactory = modelFactory;
	}

	public ProgramSpec getProgramSpec() {
		return programSpec;
	}

	public ModelFactory<?> getModelFactory() {
		return modelFactory;
	}

	public <P extends BasicProgram> RenderMaterial<P, ?> create(InstancedTileRenderer<P> renderer) {
		return new RenderMaterial<>(renderer, programSpec, modelFactory);
	}
}
