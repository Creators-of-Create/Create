package com.jozufozu.flywheel.backend.gl.attrib;

import com.jozufozu.flywheel.backend.gl.GlNumericType;

public class CommonAttributes {

	public static final VertexAttribSpec VEC4 = new VertexAttribSpec(GlNumericType.FLOAT, 4);
	public static final VertexAttribSpec VEC3 = new VertexAttribSpec(GlNumericType.FLOAT, 3);
	public static final VertexAttribSpec VEC2 = new VertexAttribSpec(GlNumericType.FLOAT, 2);
	public static final VertexAttribSpec FLOAT = new VertexAttribSpec(GlNumericType.FLOAT, 1);

	public static final VertexAttribSpec QUATERNION = new VertexAttribSpec(GlNumericType.FLOAT, 4);
	public static final VertexAttribSpec NORMAL = new VertexAttribSpec(GlNumericType.BYTE, 3, true);
	public static final VertexAttribSpec UV = new VertexAttribSpec(GlNumericType.FLOAT, 2);

	public static final VertexAttribSpec RGBA = new VertexAttribSpec(GlNumericType.UBYTE, 4, true);
	public static final VertexAttribSpec RGB = new VertexAttribSpec(GlNumericType.UBYTE, 3, true);
	public static final VertexAttribSpec LIGHT = new VertexAttribSpec(GlNumericType.UBYTE, 2, true);

	public static final VertexAttribSpec NORMALIZED_BYTE = new VertexAttribSpec(GlNumericType.BYTE, 1, true);
}
