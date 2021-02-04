package com.simibubi.create.foundation.render.gl.attrib;

import com.simibubi.create.foundation.render.gl.GlPrimitiveType;

public class CommonAttributes {

    public static final VertexAttribute MAT4 = new VertexAttribute("aMat4", GlPrimitiveType.FLOAT, 16);
    public static final VertexAttribute VEC4 = new VertexAttribute("aVec4", GlPrimitiveType.FLOAT, 4);
    public static final VertexAttribute VEC3 = new VertexAttribute("aVec3", GlPrimitiveType.FLOAT, 3);
    public static final VertexAttribute VEC2 = new VertexAttribute("aVec2", GlPrimitiveType.FLOAT, 2);
    public static final VertexAttribute FLOAT = new VertexAttribute("aFloat", GlPrimitiveType.FLOAT, 1);

    public static final VertexAttribute POSITION = VertexAttribute.copy("aPos", VEC3);
    public static final VertexAttribute NORMAL = new VertexAttribute("aNormal", GlPrimitiveType.BYTE, 3, true);
    public static final VertexAttribute UV = VertexAttribute.copy("aTexCoords", VEC2);

    public static final VertexAttribute ROTATION = VertexAttribute.copy("eulerAngles", VEC3);
    public static final VertexAttribute INSTANCE_POSITION = VertexAttribute.copy("instancePos", VEC3);

    public static final VertexAttribute RGBA = new VertexAttribute("rgba", GlPrimitiveType.UBYTE, 4, true);
    public static final VertexAttribute RGB = new VertexAttribute("rgb", GlPrimitiveType.UBYTE, 3, true);
    public static final VertexAttribute LIGHT = new VertexAttribute("light", GlPrimitiveType.UBYTE, 2, true);
}
