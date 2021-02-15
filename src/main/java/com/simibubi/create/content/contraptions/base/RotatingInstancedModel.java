package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.client.renderer.BufferBuilder;

public class RotatingInstancedModel extends InstancedModel<RotatingData> {
    public RotatingInstancedModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
        super(renderer, buf);
    }

    @Override
    protected RotatingData newInstance() {
        return new RotatingData(this);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return RotatingData.FORMAT;
    }

}
