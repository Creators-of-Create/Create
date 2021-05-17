package com.simibubi.create.foundation.render.effects;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.util.RenderUtil;

import net.minecraft.util.math.vector.Matrix4f;

public class FilterSphere {
	public float x;
	public float y;
	public float z;
	public float radius;

	public float feather;
	public float fade;
	public float density = 2;
	public boolean blend = false;

	public boolean surface = true;
	public boolean field = true;
	public float strength = 1;

	public boolean rMask;
	public boolean gMask;
	public boolean bMask;

	public Matrix4f filter;

	public void write(MappedBuffer buf) {
		buf.putFloatArray(new float[]{
				x,
				y,
				z,
				radius,

				feather,
				fade,
				density,
				blend ? 1 : 0,

				surface ? 1 : 0,
				field ? 1 : 0,
				Math.abs(strength),
				strength < 0 ? 1 : 0,

				rMask ? 1 : 0,
				gMask ? 1 : 0,
				bMask ? 1 : 0,
				0,        // padding
		});

		buf.putFloatArray(RenderUtil.writeMatrix(filter));
	}
}
