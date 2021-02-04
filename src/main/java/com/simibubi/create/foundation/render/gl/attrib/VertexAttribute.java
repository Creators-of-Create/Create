package com.simibubi.create.foundation.render.gl.attrib;

import com.simibubi.create.foundation.render.gl.GlPrimitiveType;
import org.lwjgl.opengl.GL20;

public class VertexAttribute {

    private final String name;
    private final GlPrimitiveType type;
    private final int count;
    private final int size;
    private final int attributeCount;
    private final boolean normalized;

    public static VertexAttribute copy(String name, VertexAttribute other) {
        return new VertexAttribute(name, other);
    }

    public VertexAttribute(String name, VertexAttribute that) {
        this.name = name;
        this.type = that.type;
        this.count = that.count;
        this.size = that.size;
        this.attributeCount = that.attributeCount;
        this.normalized = that.normalized;
    }

    public VertexAttribute(String name, GlPrimitiveType type, int count) {
        this(name, type, count, false);
    }

    public VertexAttribute(String name, GlPrimitiveType type, int count, boolean normalized) {
        this.name = name;
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
