package com.simibubi.create.content.contraptions.base.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;

public class UnsafeRotatingWriter extends UnsafeBufferWriter<RotatingData> {
	public UnsafeRotatingWriter(VecBuffer backingBuffer, StructType<RotatingData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	public void write(RotatingData d) {
		long addr = writePointer;
		MemoryUtil.memPutByte(addr, d.blockLight);
		MemoryUtil.memPutByte(addr + 1, d.skyLight);
		MemoryUtil.memPutByte(addr + 2, d.r);
		MemoryUtil.memPutByte(addr + 3, d.g);
		MemoryUtil.memPutByte(addr + 4, d.b);
		MemoryUtil.memPutByte(addr + 5, d.a);
		MemoryUtil.memPutFloat(addr + 6, d.x);
		MemoryUtil.memPutFloat(addr + 10, d.y);
		MemoryUtil.memPutFloat(addr + 14, d.z);
		MemoryUtil.memPutFloat(addr + 18, d.rotationalSpeed);
		MemoryUtil.memPutFloat(addr + 22, d.rotationOffset);
		MemoryUtil.memPutByte(addr + 26, d.rotationAxisX);
		MemoryUtil.memPutByte(addr + 27, d.rotationAxisY);
		MemoryUtil.memPutByte(addr + 28, d.rotationAxisZ);

		advance();
	}
}
