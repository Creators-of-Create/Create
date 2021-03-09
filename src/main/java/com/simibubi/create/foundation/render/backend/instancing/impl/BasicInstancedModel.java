package com.simibubi.create.foundation.render.backend.instancing.impl;

import com.simibubi.create.foundation.render.backend.gl.attrib.InstanceVertexAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.client.renderer.BufferBuilder;

public class BasicInstancedModel extends InstancedModel<ModelData> {
    public static final VertexFormat INSTANCE_FORMAT = VertexFormat.builder().addAttributes(InstanceVertexAttributes.class).build();

    public BasicInstancedModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
        super(renderer, buf);
    }

    @Override
    protected ModelData newInstance() {
        return new ModelData(this);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return INSTANCE_FORMAT;
    }
}
