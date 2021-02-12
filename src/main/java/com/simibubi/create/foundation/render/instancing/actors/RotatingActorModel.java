package com.simibubi.create.foundation.render.instancing.actors;

import com.simibubi.create.foundation.render.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import net.minecraft.client.renderer.BufferBuilder;

public class RotatingActorModel extends InstancedModel<StaticRotatingActorData> {
    public RotatingActorModel(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return StaticRotatingActorData.FORMAT;
    }

    @Override
    protected StaticRotatingActorData newInstance() {
        return new StaticRotatingActorData();
    }
}
