package com.simibubi.create.foundation.render.backend.instancing.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.backend.RenderUtil;
import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;

import java.nio.ByteBuffer;

public class TransformData extends InstanceData {

    private Matrix4f modelMat;
    private Matrix3f normalMat;

    private byte blockLight;
    private byte skyLight;

    public TransformData(InstancedModel<?> owner) {
        super(owner);
    }

    public TransformData setModelMat(Matrix4f modelMat) {
        this.modelMat = modelMat;
        return this;
    }

    public TransformData setNormalMat(Matrix3f normalMat) {
        this.normalMat = normalMat;
        return this;
    }

    public TransformData setTransform(MatrixStack stack) {
        this.modelMat = stack.peek().getModel();
        this.normalMat = stack.peek().getNormal();
        return this;
    }

    public TransformData setBlockLight(byte blockLight) {
        this.blockLight = blockLight;
        return this;
    }

    public TransformData setSkyLight(byte skyLight) {
        this.skyLight = skyLight;
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        RenderUtil.writeMat4(buf, modelMat);
        RenderUtil.writeMat3(buf, normalMat);
        buf.put(new byte[] { blockLight, skyLight });
    }
}
