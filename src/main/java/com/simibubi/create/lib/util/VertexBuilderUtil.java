package com.simibubi.create.lib.util;

import java.nio.ByteBuffer;

import com.mojang.math.Matrix3f;
import com.mojang.math.Vector3f;

public class VertexBuilderUtil {
	public static int applyBakedLighting(int lightmapCoord, ByteBuffer data) {
		int bl = lightmapCoord & 0xFFFF;
		int sl = (lightmapCoord >> 16) & 0xFFFF;
		int offset = LightUtil.getLightOffset(0) * 4;
		int blBaked = Short.toUnsignedInt(data.getShort(offset));
		int slBaked = Short.toUnsignedInt(data.getShort(offset + 2));
		bl = Math.max(bl, blBaked);
		sl = Math.max(sl, slBaked);
		return bl | (sl << 16);
	}

	public static void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
		byte nx = data.get(28);
		byte ny = data.get(29);
		byte nz = data.get(30);
		if (nx != 0 || ny != 0 || nz != 0) {
			generated.set(nx / 127f, ny / 127f, nz / 127f);
			generated.transform(normalTransform);
		}
	}
}
