package com.simibubi.create.content.contraptions.actors.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.InstanceWriter;

public class UnsafeActorWriter implements InstanceWriter<ActorInstance> {
	public static final UnsafeActorWriter INSTANCE = new UnsafeActorWriter();

	@Override
    public void write(long ptr, ActorInstance d) {
        MemoryUtil.memPutFloat(ptr, d.x);
		MemoryUtil.memPutFloat(ptr + 4, d.y);
		MemoryUtil.memPutFloat(ptr + 8, d.z);
		MemoryUtil.memPutByte(ptr + 12, d.blockLight);
		MemoryUtil.memPutByte(ptr + 13, d.skyLight);
		MemoryUtil.memPutFloat(ptr + 14, d.rotationOffset);
		MemoryUtil.memPutByte(ptr + 18, d.rotationAxisX);
		MemoryUtil.memPutByte(ptr + 19, d.rotationAxisY);
		MemoryUtil.memPutByte(ptr + 20, d.rotationAxisZ);
		MemoryUtil.memPutFloat(ptr + 21, d.qX);
		MemoryUtil.memPutFloat(ptr + 25, d.qY);
		MemoryUtil.memPutFloat(ptr + 29, d.qZ);
		MemoryUtil.memPutFloat(ptr + 33, d.qW);
		MemoryUtil.memPutByte(ptr + 37, d.rotationCenterX);
		MemoryUtil.memPutByte(ptr + 38, d.rotationCenterY);
		MemoryUtil.memPutByte(ptr + 39, d.rotationCenterZ);
		MemoryUtil.memPutFloat(ptr + 40, d.speed);
	}
}
