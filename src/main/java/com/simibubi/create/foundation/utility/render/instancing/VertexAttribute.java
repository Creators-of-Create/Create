package com.simibubi.create.foundation.utility.render.instancing;

import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL20;

public class VertexAttribute {
    public static final VertexAttribute MAT4 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 16);
    public static final VertexAttribute VEC4 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 4);
    public static final VertexAttribute VEC3 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 3);
    public static final VertexAttribute VEC2 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 2);
    public static final VertexAttribute FLOAT = new VertexAttribute(VertexFormatElement.Type.FLOAT, 1);

    public static final VertexAttribute POSITION = VEC3;
    public static final VertexAttribute NORMAL = new VertexAttribute(VertexFormatElement.Type.BYTE, 3, true);
    public static final VertexAttribute RGBA = new VertexAttribute(VertexFormatElement.Type.BYTE, 4, true);
    public static final VertexAttribute RGB = new VertexAttribute(VertexFormatElement.Type.BYTE, 3, true);
    public static final VertexAttribute UV = VEC2;
    public static final VertexAttribute LIGHT = new VertexAttribute(VertexFormatElement.Type.FLOAT, 2);

    private final VertexFormatElement.Type type;
    private final int count;
    private final int size;
    private final int attributeCount;
    private final boolean normalized;

    public VertexAttribute(VertexFormatElement.Type type, int count) {
        this(type, count, false);
    }

    public VertexAttribute(VertexFormatElement.Type type, int count, boolean normalized) {
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
