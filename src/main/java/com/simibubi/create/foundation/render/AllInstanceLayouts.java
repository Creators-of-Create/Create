package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.lib.layout.BufferLayout;
import com.jozufozu.flywheel.lib.layout.CommonItems;

public class AllInstanceLayouts {

	public static final BufferLayout ROTATING = kineticInstance()
			.addItems(CommonItems.NORMAL)
			.build();

	public static final BufferLayout BELT = kineticInstance()
			.addItems(CommonItems.QUATERNION, CommonItems.UV, CommonItems.VEC4,
					CommonItems.NORMALIZED_BYTE)
			.build();

	public static final BufferLayout ACTOR = BufferLayout.builder()
			.addItems(CommonItems.VEC3, CommonItems.LIGHT, CommonItems.FLOAT,
					CommonItems.NORMAL, CommonItems.QUATERNION, CommonItems.NORMAL,
					CommonItems.FLOAT)
			.build();

	public static final BufferLayout FLAP = BufferLayout.builder()
			.addItems(CommonItems.VEC3, CommonItems.LIGHT, CommonItems.VEC3, CommonItems.VEC3,
					CommonItems.FLOAT, CommonItems.FLOAT, CommonItems.FLOAT, CommonItems.FLOAT)
			.build();

	private static BufferLayout.Builder kineticInstance() {
		return BufferLayout.builder()
				.addItems(CommonItems.LIGHT, CommonItems.RGBA)
				.addItems(CommonItems.VEC3, CommonItems.FLOAT, CommonItems.FLOAT);
	}
}
