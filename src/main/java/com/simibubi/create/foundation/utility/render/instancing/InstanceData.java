package com.simibubi.create.foundation.utility.render.instancing;

import java.nio.ByteBuffer;

public abstract class InstanceData {

    public abstract void write(ByteBuffer buf);

    public void putVec4(ByteBuffer buf, float x, float y, float z, float w) {
        putFloat(buf, x);
        putFloat(buf, y);
        putFloat(buf, z);
        putFloat(buf, w);
    }

    public void putVec3(ByteBuffer buf, float x, float y, float z) {
        putFloat(buf, x);
        putFloat(buf, y);
        putFloat(buf, z);
    }

    public void putVec2(ByteBuffer buf, float x, float y) {
        putFloat(buf, x);
        putFloat(buf, y);
    }

    public void putFloat(ByteBuffer buf, float f) {
        buf.putFloat(f);
    }
}
