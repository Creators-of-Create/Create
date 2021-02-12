package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import net.minecraft.client.renderer.BufferBuilder;

public class RotatingActorModel extends InstancedModel<ContraptionActorData> {
    public RotatingActorModel(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return ContraptionActorData.FORMAT;
    }

    @Override
    protected ContraptionActorData newInstance() {
        return new ContraptionActorData();
    }
}
