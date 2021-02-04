package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.gl.attrib.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;

public class BeltModel extends InstancedModel<BeltData> {
    public BeltModel(BufferBuilder buf) {
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
