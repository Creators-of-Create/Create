package com.simibubi.create.foundation.render.backend.instancing.impl;

import com.simibubi.create.foundation.render.backend.gl.attrib.InstanceVertexAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.client.renderer.BufferBuilder;

public class TransformedInstancedModel extends InstancedModel<TransformData> {
    public static final VertexFormat INSTANCE_FORMAT = VertexFormat.builder().addAttributes(InstanceVertexAttributes.class).build();

    public TransformedInstancedModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
        super(renderer, buf);
    }

    @Override
    protected TransformData newInstance() {
        return new TransformData(this);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return INSTANCE_FORMAT;
    }
}
