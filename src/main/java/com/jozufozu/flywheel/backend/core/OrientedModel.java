package com.jozufozu.flywheel.backend.core;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;

import net.minecraft.client.renderer.BufferBuilder;

public class OrientedModel extends InstancedModel<OrientedData> {
	public static final VertexFormat INSTANCE_FORMAT = VertexFormat.builder()
			.addAttributes(BasicAttributes.class)
			.addAttributes(OrientedAttributes.class)
			.build();

	public OrientedModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
		super(renderer, buf);
	}

	@Override
	protected OrientedData newInstance() {
		return new OrientedData(this);
	}

	@Override
	protected VertexFormat getInstanceFormat() {
		return INSTANCE_FORMAT;
	}
}
