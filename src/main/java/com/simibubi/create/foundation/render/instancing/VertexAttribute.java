package com.simibubi.create.foundation.render.instancing;

import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL20;

public class VertexAttribute {
    public static final VertexAttribute MAT4 = new VertexAttribute("mat4", VertexFormatElement.Type.FLOAT, 16);
    public static final VertexAttribute VEC4 = new VertexAttribute("vec4", VertexFormatElement.Type.FLOAT, 4);
    public static final VertexAttribute VEC3 = new VertexAttribute("vec3", VertexFormatElement.Type.FLOAT, 3);
    public static final VertexAttribute VEC2 = new VertexAttribute("vec2", VertexFormatElement.Type.FLOAT, 2);
    public static final VertexAttribute FLOAT = new VertexAttribute("float", VertexFormatElement.Type.FLOAT, 1);

    public static final VertexAttribute POSITION = copy("pos", VEC3);
    public static final VertexAttribute INSTANCE_POSITION = copy("instancePos", VEC3);
    public static final VertexAttribute ROTATION = copy("eulerAngles", VEC3);
    public static final VertexAttribute NORMAL = new VertexAttribute("normal", VertexFormatElement.Type.BYTE, 3, true);
    public static final VertexAttribute RGBA = new VertexAttribute("rgba", VertexFormatElement.Type.UBYTE, 4, true);
    public static final VertexAttribute RGB = new VertexAttribute("rgb", VertexFormatElement.Type.UBYTE, 3, true);
    public static final VertexAttribute UV = copy("uv", VEC2);
    public static final VertexAttribute LIGHT = new VertexAttribute("light", VertexFormatElement.Type.UBYTE, 2, true);

    private final String name;
    private final VertexFormatElement.Type type;
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

    public VertexAttribute(String name, VertexFormatElement.Type type, int count) {
        this(name, type, count, false);
    }

    public VertexAttribute(String name, VertexFormatElement.Type type, int count, boolean normalized) {
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
