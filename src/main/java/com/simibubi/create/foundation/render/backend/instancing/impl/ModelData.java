package com.simibubi.create.foundation.render.backend.instancing.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.backend.RenderUtil;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

import java.nio.ByteBuffer;

public class ModelData extends BasicData {
    private static final float[] empty = new float[25];

    private float[] matrices = empty;

    public ModelData(InstancedModel<?> owner) {
        super(owner);
    }

    public ModelData setTransform(MatrixStack stack) {
        matrices = RenderUtil.bufferMatrices(stack.peek().getModel(), stack.peek().getNormal());
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        super.write(buf);
        buf.asFloatBuffer().put(matrices);
        buf.position(buf.position() + matrices.length * 4);
    }
}
