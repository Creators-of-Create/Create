package com.simibubi.create.content.kinetics.base.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public class RotatingWriterUnsafe extends KineticWriterUnsafe<RotatingData> {
	public RotatingWriterUnsafe(VecBuffer backingBuffer, StructType<RotatingData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	protected void writeInternal(RotatingData d) {
		super.writeInternal(d);
		long addr = writePointer;
		MemoryUtil.memPutByte(addr + 26, d.rotationAxisX);
		MemoryUtil.memPutByte(addr + 27, d.rotationAxisY);
		MemoryUtil.memPutByte(addr + 28, d.rotationAxisZ);
	}
}
