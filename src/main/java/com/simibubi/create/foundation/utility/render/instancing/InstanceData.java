package com.simibubi.create.foundation.utility.render.instancing;

import com.simibubi.create.foundation.utility.render.SafeDirectBuffer;

import java.nio.ByteBuffer;

public abstract class InstanceData {

    public abstract void write(SafeDirectBuffer buf);

    public void putVec4(SafeDirectBuffer buf, float x, float y, float z, float w) {
        putFloat(buf, x);
        putFloat(buf, y);
        putFloat(buf, z);
        putFloat(buf, w);
    }

    public void putVec3(SafeDirectBuffer buf, float x, float y, float z) {
        putFloat(buf, x);
        putFloat(buf, y);
        putFloat(buf, z);
    }

    public void putVec2(SafeDirectBuffer buf, float x, float y) {
        putFloat(buf, x);
        putFloat(buf, y);
    }

    public void putFloat(SafeDirectBuffer buf, float f) {
        buf.putFloat(f);
    }
}
