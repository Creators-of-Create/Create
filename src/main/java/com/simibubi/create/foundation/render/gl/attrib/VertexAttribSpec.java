package com.simibubi.create.foundation.render.gl.attrib;

import com.simibubi.create.foundation.render.gl.GlPrimitiveType;
import org.lwjgl.opengl.GL20;

public class VertexAttribSpec {

    private final GlPrimitiveType type;
    private final int count;
    private final int size;
    private final int attributeCount;
    private final boolean normalized;

    public static VertexAttribSpec copy(VertexAttribSpec other) {
        return new VertexAttribSpec(other);
    }

    public VertexAttribSpec(VertexAttribSpec that) {
        this.type = that.type;
        this.count = that.count;
        this.size = that.size;
        this.attributeCount = that.attributeCount;
        this.normalized = that.normalized;
    }

    public VertexAttribSpec(GlPrimitiveType type, int count) {
        this(type, count, false);
    }

    public VertexAttribSpec(GlPrimitiveType type, int count, boolean normalized) {
        this.type = type;
        this.count = count;
        this.size = type.getSize() * count;
        this.attributeCount = (this.size + 15) / 16; // ceiling division. GLSL vertex attributes can only be 16 bytes wide
        this.normalized = normalized;
    }

    public void registerForBuffer(int stride, int indexAcc, int offsetAcc) {
        GL20.glVertexAttribPointer(indexAcc, count, type.getGlConstant(), normalized, stride, offsetAcc);
    }

    public int getSize() {
        return size;
    }

    public int getAttributeCount() {
        return attributeCount;
    }
}
