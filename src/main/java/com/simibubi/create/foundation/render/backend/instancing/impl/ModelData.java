package com.simibubi.create.foundation.render.backend.instancing.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.backend.RenderUtil;
import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;

import java.nio.ByteBuffer;

public class ModelData extends InstanceData {
    private static final Matrix4f IDENT4 = new Matrix4f();
    private static final Matrix3f IDENT3 = new Matrix3f();
    static {
        IDENT4.loadIdentity();
        IDENT3.loadIdentity();
    }

    private Matrix4f modelMat = IDENT4;
    private Matrix3f normalMat = IDENT3;

    private byte blockLight;
    private byte skyLight;

    private byte r = (byte) 0xFF;
    private byte g = (byte) 0xFF;
    private byte b = (byte) 0xFF;
    private byte a = (byte) 0xFF;

    public ModelData(InstancedModel<?> owner) {
        super(owner);
    }

    public ModelData setModelMat(Matrix4f modelMat) {
        this.modelMat = modelMat;
        return this;
    }

    public ModelData setNormalMat(Matrix3f normalMat) {
        this.normalMat = normalMat;
        return this;
    }

    public ModelData setTransform(MatrixStack stack) {
        this.modelMat = stack.peek().getModel().copy();
        this.normalMat = stack.peek().getNormal().copy();
        return this;
    }

    public ModelData setTransformNoCopy(MatrixStack stack) {
        this.modelMat = stack.peek().getModel();
        this.normalMat = stack.peek().getNormal();
        return this;
    }

    public ModelData setBlockLight(int blockLight) {
        this.blockLight = (byte) (blockLight << 4);
        return this;
    }

    public ModelData setSkyLight(int skyLight) {
        this.skyLight = (byte) (skyLight << 4);
        return this;
    }

    public ModelData setColor(int color) {
        byte a = (byte) ((color >> 24) & 0xFF);
        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8) & 0xFF);
        byte b = (byte) (color & 0xFF);
        return setColor(r, g, b);
    }

    public ModelData setColor(int r, int g, int b) {
        return setColor((byte) r, (byte) g, (byte) b);
    }

    public ModelData setColor(byte r, byte g, byte b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }

    public ModelData setColor(byte r, byte g, byte b, byte a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        RenderUtil.writeMat4(buf, modelMat);
        RenderUtil.writeMat3(buf, normalMat);
        buf.put(new byte[] { blockLight, skyLight, r, g, b, a });
    }
}
