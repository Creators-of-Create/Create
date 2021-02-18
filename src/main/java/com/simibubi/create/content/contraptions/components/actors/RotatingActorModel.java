package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.client.renderer.BufferBuilder;

public class RotatingActorModel extends InstancedModel<ContraptionActorData> {
    public RotatingActorModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
        super(renderer, buf);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return ContraptionActorData.FORMAT;
    }

    @Override
    protected ContraptionActorData newInstance() {
        return new ContraptionActorData(this);
    }
}
