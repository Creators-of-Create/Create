package com.simibubi.create.content.logistics.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.InstanceWriter;

public class FlapWriter implements InstanceWriter<FlapData> {
	public static final FlapWriter INSTANCE = new FlapWriter();

	@Override
    public void write(long ptr, FlapData d) {
        MemoryUtil.memPutFloat(ptr, d.x);
		MemoryUtil.memPutFloat(ptr + 4, d.y);
		MemoryUtil.memPutFloat(ptr + 8, d.z);
		MemoryUtil.memPutByte(ptr + 12, (byte) (d.blockLight << 4));
		MemoryUtil.memPutByte(ptr + 13, (byte) (d.skyLight << 4));
		MemoryUtil.memPutFloat(ptr + 14, d.segmentOffsetX);
		MemoryUtil.memPutFloat(ptr + 18, d.segmentOffsetY);
		MemoryUtil.memPutFloat(ptr + 22, d.segmentOffsetZ);
		MemoryUtil.memPutFloat(ptr + 26, d.pivotX);
		MemoryUtil.memPutFloat(ptr + 30, d.pivotY);
		MemoryUtil.memPutFloat(ptr + 34, d.pivotZ);
		MemoryUtil.memPutFloat(ptr + 38, d.horizontalAngle);
		MemoryUtil.memPutFloat(ptr + 42, d.intensity);
		MemoryUtil.memPutFloat(ptr + 46, d.flapScale);
		MemoryUtil.memPutFloat(ptr + 50, d.flapness);
	}
}
