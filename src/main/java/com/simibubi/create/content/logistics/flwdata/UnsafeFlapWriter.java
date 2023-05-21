package com.simibubi.create.content.logistics.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;

public class UnsafeFlapWriter extends UnsafeBufferWriter<FlapData> {
	public UnsafeFlapWriter(VecBuffer backingBuffer, StructType<FlapData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
    protected void writeInternal(FlapData d) {
		long addr = writePointer;
		MemoryUtil.memPutFloat(addr, d.x);
		MemoryUtil.memPutFloat(addr + 4, d.y);
		MemoryUtil.memPutFloat(addr + 8, d.z);
		MemoryUtil.memPutByte(addr + 12, (byte) (d.blockLight << 4));
		MemoryUtil.memPutByte(addr + 13, (byte) (d.skyLight << 4));
		MemoryUtil.memPutFloat(addr + 14, d.segmentOffsetX);
		MemoryUtil.memPutFloat(addr + 18, d.segmentOffsetY);
		MemoryUtil.memPutFloat(addr + 22, d.segmentOffsetZ);
		MemoryUtil.memPutFloat(addr + 26, d.pivotX);
		MemoryUtil.memPutFloat(addr + 30, d.pivotY);
		MemoryUtil.memPutFloat(addr + 34, d.pivotZ);
		MemoryUtil.memPutFloat(addr + 38, d.horizontalAngle);
		MemoryUtil.memPutFloat(addr + 42, d.intensity);
		MemoryUtil.memPutFloat(addr + 46, d.flapScale);
		MemoryUtil.memPutFloat(addr + 50, d.flapness);
	}
}
