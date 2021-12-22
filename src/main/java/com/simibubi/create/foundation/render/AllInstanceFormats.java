package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public class AllInstanceFormats {

	public static VertexFormat ROTATING = kineticInstance()
			.addAttributes(CommonAttributes.NORMAL)
			.build();

	public static VertexFormat BELT = kineticInstance()
			.addAttributes(CommonAttributes.QUATERNION, CommonAttributes.UV, CommonAttributes.VEC4,
					CommonAttributes.NORMALIZED_BYTE)
			.build();

	public static VertexFormat ACTOR = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3, CommonAttributes.LIGHT, CommonAttributes.FLOAT,
					CommonAttributes.NORMAL, CommonAttributes.QUATERNION, CommonAttributes.NORMAL,
					CommonAttributes.FLOAT)
			.build();

	public static VertexFormat FLAP = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3, CommonAttributes.LIGHT, CommonAttributes.VEC3, CommonAttributes.VEC3,
					CommonAttributes.FLOAT, CommonAttributes.FLOAT, CommonAttributes.FLOAT, CommonAttributes.FLOAT)
			.build();

	private static VertexFormat.Builder kineticInstance() {
		return VertexFormat.builder()
				.addAttributes(CommonAttributes.LIGHT, CommonAttributes.RGBA)
				.addAttributes(CommonAttributes.VEC3, CommonAttributes.FLOAT, CommonAttributes.FLOAT);
	}
}
