package com.simibubi.create.foundation.render.backend.instancing.impl;

import java.nio.ByteBuffer;
import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

public class BasicData extends InstanceData implements IFlatLight<BasicData> {

    protected byte blockLight;
    protected byte skyLight;

    protected byte r = (byte) 0xFF;
    protected byte g = (byte) 0xFF;
    protected byte b = (byte) 0xFF;
    protected byte a = (byte) 0xFF;

    public BasicData(InstancedModel<?> owner) {
        super(owner);
    }

    @Override
    public BasicData setBlockLight(int blockLight) {
        this.blockLight = (byte) (blockLight << 4);
        return this;
    }

    @Override
    public BasicData setSkyLight(int skyLight) {
        this.skyLight = (byte) (skyLight << 4);
        return this;
    }

    public BasicData setColor(int color) {
        return setColor(color, false);
    }

    public BasicData setColor(int color, boolean alpha) {
        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8) & 0xFF);
        byte b = (byte) (color & 0xFF);

        if (alpha) {
            byte a = (byte) ((color >> 24) & 0xFF);
            return setColor(r, g, b, a);
        } else {
            return setColor(r, g, b);
        }
    }

    public BasicData setColor(int r, int g, int b) {
        return setColor((byte) r, (byte) g, (byte) b);
    }

    public BasicData setColor(byte r, byte g, byte b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }

    public BasicData setColor(byte r, byte g, byte b, byte a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        buf.put(new byte[] { blockLight, skyLight, r, g, b, a });
    }
}
