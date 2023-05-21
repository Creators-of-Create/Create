package com.simibubi.create.content.kinetics.base.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public class BeltWriterUnsafe extends KineticWriterUnsafe<BeltData> {
	public BeltWriterUnsafe(VecBuffer backingBuffer, StructType<BeltData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	protected void writeInternal(BeltData d) {
		super.writeInternal(d);
		long addr = writePointer;
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
	}
}
