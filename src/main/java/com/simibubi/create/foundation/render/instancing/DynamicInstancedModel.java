package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.gl.GlBuffer;
import net.minecraft.client.renderer.BufferBuilder;

public abstract class DynamicInstancedModel<S extends InstanceData, D extends InstanceData> extends InstancedModel<S> {

    protected GlBuffer dynamicVBO;

    protected int dynamicBufferSize = -1;

    public DynamicInstancedModel(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected void setup() {
        super.setup();
        dynamicVBO = new GlBuffer();
    }

    protected abstract VertexFormat getDynamicFormat();

    protected abstract D newDynamicPart();

    @Override
    protected int getTotalShaderAttributeCount() {
        return super.getTotalShaderAttributeCount() + getDynamicFormat().getShaderAttributeCount();
    }

    @Override
    protected void preDrawTask() {
        super.preDrawTask();
    }

    @Override
    protected void deleteInternal() {
        super.deleteInternal();
        dynamicVBO.delete();
    }
}
