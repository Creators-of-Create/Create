package com.simibubi.create.foundation.render.backend.core;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

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
