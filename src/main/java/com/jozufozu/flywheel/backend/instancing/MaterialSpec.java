package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.core.BasicProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;

import net.minecraft.util.ResourceLocation;

public class MaterialSpec<M extends InstancedModel<?>> {

	public final ResourceLocation name;

	private final ProgramSpec programSpec;
	private final ModelFactory<M> modelFactory;

	public MaterialSpec(ResourceLocation name, ProgramSpec programSpec, ModelFactory<M> modelFactory) {
		this.name = name;
		this.programSpec = programSpec;
		this.modelFactory = modelFactory;
	}

	public ProgramSpec getProgramSpec() {
		return programSpec;
	}

	public ModelFactory<M> getModelFactory() {
		return modelFactory;
	}

	public <P extends BasicProgram> RenderMaterial<P, M> create(InstancedTileRenderer<P> renderer) {
		return new RenderMaterial<>(renderer, programSpec, modelFactory);
	}
}
