package com.simibubi.create.content.kinetics.base.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.materials.BasicWriterUnsafe;

public abstract class KineticWriterUnsafe<D extends KineticData> extends BasicWriterUnsafe<D> {
	public KineticWriterUnsafe(VecBuffer backingBuffer, StructType<D> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	protected void writeInternal(D d) {
		super.writeInternal(d);
		long addr = writePointer;
		MemoryUtil.memPutFloat(addr + 6, d.x);
		MemoryUtil.memPutFloat(addr + 10, d.y);
		MemoryUtil.memPutFloat(addr + 14, d.z);
		MemoryUtil.memPutFloat(addr + 18, d.rotationalSpeed);
		MemoryUtil.memPutFloat(addr + 22, d.rotationOffset);
	}
}
