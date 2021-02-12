package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import net.minecraft.client.renderer.BufferBuilder;

public class RotatingInstancedModel extends InstancedModel<RotatingData> {
    public RotatingInstancedModel(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected RotatingData newInstance() {
        return new RotatingData();
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return RotatingData.FORMAT;
    }

}
