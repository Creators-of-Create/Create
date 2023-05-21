package com.simibubi.create.content.contraptions.actors.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;

public class UnsafeActorWriter extends UnsafeBufferWriter<ActorData> {
	public UnsafeActorWriter(VecBuffer backingBuffer, StructType<ActorData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
    protected void writeInternal(ActorData d) {
		long addr = writePointer;
		MemoryUtil.memPutFloat(addr, d.x);
		MemoryUtil.memPutFloat(addr + 4, d.y);
		MemoryUtil.memPutFloat(addr + 8, d.z);
		MemoryUtil.memPutByte(addr + 12, d.blockLight);
		MemoryUtil.memPutByte(addr + 13, d.skyLight);
		MemoryUtil.memPutFloat(addr + 14, d.rotationOffset);
		MemoryUtil.memPutByte(addr + 18, d.rotationAxisX);
		MemoryUtil.memPutByte(addr + 19, d.rotationAxisY);
		MemoryUtil.memPutByte(addr + 20, d.rotationAxisZ);
		MemoryUtil.memPutFloat(addr + 21, d.qX);
		MemoryUtil.memPutFloat(addr + 25, d.qY);
		MemoryUtil.memPutFloat(addr + 29, d.qZ);
		MemoryUtil.memPutFloat(addr + 33, d.qW);
		MemoryUtil.memPutByte(addr + 37, d.rotationCenterX);
		MemoryUtil.memPutByte(addr + 38, d.rotationCenterY);
		MemoryUtil.memPutByte(addr + 39, d.rotationCenterZ);
		MemoryUtil.memPutFloat(addr + 40, d.speed);
	}
}
