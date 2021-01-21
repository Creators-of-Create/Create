package com.simibubi.create.foundation.render.instancing.actors;

import com.simibubi.create.foundation.render.instancing.DynamicInstanceBuffer;
import com.simibubi.create.foundation.render.instancing.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;

public class RotatingActorBuffer extends DynamicInstanceBuffer<StaticRotatingActorData, DynamicRotatingActorData> {
    public RotatingActorBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected VertexFormat getDynamicFormat() {
        return DynamicRotatingActorData.FORMAT;
    }

    @Override
    protected DynamicRotatingActorData newDynamicPart() {
        return new DynamicRotatingActorData();
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
