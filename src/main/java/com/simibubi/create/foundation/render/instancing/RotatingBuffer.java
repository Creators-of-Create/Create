package com.simibubi.create.foundation.render.instancing;

import net.minecraft.client.renderer.BufferBuilder;

public class RotatingBuffer extends InstanceBuffer<RotatingData> {
    public RotatingBuffer(BufferBuilder buf) {
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
