package com.simibubi.create.foundation.render.instancing;

import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.*;

public abstract class DynamicInstanceBuffer<S extends InstanceData, D extends InstanceData> extends InstanceBuffer<S> {

    protected int dynamicVBO;

    protected int dynamicBufferSize = -1;

    public DynamicInstanceBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected void setup() {
        super.setup();
        dynamicVBO = GL20.glGenBuffers();
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
}
