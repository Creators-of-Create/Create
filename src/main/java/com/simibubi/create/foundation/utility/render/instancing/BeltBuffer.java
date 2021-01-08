package com.simibubi.create.foundation.utility.render.instancing;

import net.minecraft.client.renderer.BufferBuilder;

public class BeltBuffer extends InstanceBuffer<BeltData> {
    public BeltBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected BeltData newInstance() {
        return new BeltData();
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return BeltData.FORMAT;
    }

}
