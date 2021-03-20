package com.simibubi.create.content.logistics.block;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.client.renderer.BufferBuilder;

public class FlapInstancedModel extends InstancedModel<FlapData> {
    public FlapInstancedModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
        super(renderer, buf);
    }

    @Override
    protected FlapData newInstance() {
        return new FlapData(this);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return FlapData.FORMAT;
    }
}
