package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import net.minecraft.client.renderer.BufferBuilder;

public class BeltInstancedModel extends InstancedModel<BeltData> {
    public BeltInstancedModel(BufferBuilder buf) {
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
