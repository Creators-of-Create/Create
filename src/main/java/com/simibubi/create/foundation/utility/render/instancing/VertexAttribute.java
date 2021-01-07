package com.simibubi.create.foundation.utility.render.instancing;

import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL20;

public class VertexAttribute {

    public static VertexAttribute MAT4 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 16);
    public static VertexAttribute VEC4 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 4);
    public static VertexAttribute VEC3 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 3);
    public static VertexAttribute VEC2 = new VertexAttribute(VertexFormatElement.Type.FLOAT, 2);
    public static VertexAttribute FLOAT = new VertexAttribute(VertexFormatElement.Type.FLOAT, 1);

    private final VertexFormatElement.Type type;
    private final int count;
    private final int size;
    private final int attributeCount;

    public VertexAttribute(VertexFormatElement.Type type, int count) {
        this.type = type;
        this.count = count;
        this.size = type.getSize() * count;
        this.attributeCount = (this.size + 15) / 16; // ceiling division. GLSL vertex attributes can only be 16 bytes wide
    }

    public void registerForBuffer(int stride, int indexAcc, int offsetAcc) {
        GL20.glVertexAttribPointer(indexAcc, count, type.getGlConstant(), false, stride, offsetAcc);
    }

    public int getSize() {
        return size;
    }

    public int getAttributeCount() {
        return attributeCount;
    }
}
