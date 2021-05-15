package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;

import net.minecraft.util.ResourceLocation;

public class MaterialSpec<D extends InstanceData> {

	public final ResourceLocation name;

	private final ProgramSpec programSpec;
	private final VertexFormat instanceFormat;
	private final InstanceFactory<D> instanceFactory;

	public MaterialSpec(ResourceLocation name, ProgramSpec programSpec, VertexFormat instanceFormat, InstanceFactory<D> instanceFactory) {
		this.name = name;
		this.programSpec = programSpec;
		this.instanceFormat = instanceFormat;
		this.instanceFactory = instanceFactory;
	}

	public ProgramSpec getProgramSpec() {
		return programSpec;
	}

	public VertexFormat getInstanceFormat() {
		return instanceFormat;
	}

	public InstanceFactory<D> getInstanceFactory() {
		return instanceFactory;
	}

}
