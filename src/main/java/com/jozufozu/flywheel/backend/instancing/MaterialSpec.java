package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

import net.minecraft.util.ResourceLocation;

public class MaterialSpec<D extends InstanceData> {

	public final ResourceLocation name;

	private final ResourceLocation programSpec;
	private final VertexFormat modelFormat;
	private final VertexFormat instanceFormat;
	private final IInstanceFactory<D> instanceFactory;

	public MaterialSpec(ResourceLocation name, ResourceLocation programSpec, VertexFormat modelFormat, VertexFormat instanceFormat, IInstanceFactory<D> instanceFactory) {
		this.name = name;
		this.programSpec = programSpec;
		this.modelFormat = modelFormat;
		this.instanceFormat = instanceFormat;
		this.instanceFactory = instanceFactory;
	}

	public ResourceLocation getProgramName() {
		return programSpec;
	}

	public VertexFormat getModelFormat() {
		return modelFormat;
	}

	public VertexFormat getInstanceFormat() {
		return instanceFormat;
	}

	public IInstanceFactory<D> getInstanceFactory() {
		return instanceFactory;
	}

}
