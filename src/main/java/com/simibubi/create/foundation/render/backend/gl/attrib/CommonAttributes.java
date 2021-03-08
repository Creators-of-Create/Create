package com.simibubi.create.foundation.render.backend.gl.attrib;

import com.simibubi.create.foundation.render.backend.gl.GlPrimitiveType;

public class CommonAttributes {

    public static final VertexAttribSpec VEC4 = new VertexAttribSpec(GlPrimitiveType.FLOAT, 4);
    public static final VertexAttribSpec VEC3 = new VertexAttribSpec(GlPrimitiveType.FLOAT, 3);
    public static final VertexAttribSpec VEC2 = new VertexAttribSpec(GlPrimitiveType.FLOAT, 2);
    public static final VertexAttribSpec FLOAT = new VertexAttribSpec(GlPrimitiveType.FLOAT, 1);

    public static final VertexAttribSpec NORMAL = new VertexAttribSpec(GlPrimitiveType.BYTE, 3, true);
    public static final VertexAttribSpec UV = new VertexAttribSpec(GlPrimitiveType.FLOAT, 2);

    public static final VertexAttribSpec RGBA = new VertexAttribSpec(GlPrimitiveType.UBYTE, 4, true);
    public static final VertexAttribSpec RGB = new VertexAttribSpec(GlPrimitiveType.UBYTE, 3, true);
    public static final VertexAttribSpec LIGHT = new VertexAttribSpec(GlPrimitiveType.UBYTE, 2, true);

    public static final VertexAttribSpec NORMALIZED_BYTE = new VertexAttribSpec(GlPrimitiveType.BYTE, 1, true);
}
