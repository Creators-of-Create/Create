package com.simibubi.create.content.contraptions.base.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;

public class UnsafeBeltWriter extends UnsafeBufferWriter<BeltData> {
	public UnsafeBeltWriter(VecBuffer backingBuffer, StructType<BeltData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	public void write(BeltData d) {
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
		MemoryUtil.memPutFloat(addr + 26, d.qX);
		MemoryUtil.memPutFloat(addr + 30, d.qY);
		MemoryUtil.memPutFloat(addr + 34, d.qZ);
		MemoryUtil.memPutFloat(addr + 38, d.qW);
		MemoryUtil.memPutFloat(addr + 42, d.sourceU);
		MemoryUtil.memPutFloat(addr + 46, d.sourceV);
		MemoryUtil.memPutFloat(addr + 50, d.minU);
		MemoryUtil.memPutFloat(addr + 54, d.minV);
		MemoryUtil.memPutFloat(addr + 58, d.maxU);
		MemoryUtil.memPutFloat(addr + 62, d.maxV);
		MemoryUtil.memPutByte(addr + 66, d.scrollMult);

		advance();
	}
}
