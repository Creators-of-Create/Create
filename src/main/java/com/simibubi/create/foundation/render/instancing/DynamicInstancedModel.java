package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.gl.GlBuffer;
import com.simibubi.create.foundation.render.gl.attrib.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.GL20;

public abstract class DynamicInstancedModel<S extends InstanceData, D extends InstanceData> extends InstancedModel<S> {

    protected GlBuffer dynamicVBO;

    protected int dynamicBufferSize = -1;

    public DynamicInstancedModel(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected void init() {
        super.init();
        dynamicVBO = new GlBuffer(GL20.GL_ARRAY_BUFFER);
    }

    protected abstract VertexFormat getDynamicFormat();

    protected abstract D newDynamicPart();

    @Override
    protected int getTotalShaderAttributeCount() {
        return super.getTotalShaderAttributeCount() + getDynamicFormat().getShaderAttributeCount();
    }

    @Override
    protected void deleteInternal() {
        super.deleteInternal();
        dynamicVBO.delete();
    }
}
