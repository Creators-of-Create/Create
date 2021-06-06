package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.MatrixAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public class Formats {
	public static final VertexFormat UNLIT_MODEL = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3, CommonAttributes.NORMAL, CommonAttributes.UV)
			.build();

	public static final VertexFormat TRANSFORMED = litInstance()
			.addAttributes(MatrixAttributes.MAT4,
					MatrixAttributes.MAT3)
			.build();
	public static final VertexFormat ORIENTED = litInstance()
			.addAttributes(CommonAttributes.VEC3, CommonAttributes.VEC3, CommonAttributes.QUATERNION)
			.build();

	public static VertexFormat.Builder litInstance() {
		return VertexFormat.builder()
				.addAttributes(CommonAttributes.LIGHT, CommonAttributes.RGBA);
	}
}
