package com.simibubi.create.content.kinetics.base.flwdata;

import org.lwjgl.system.MemoryUtil;

public class BeltWriter extends KineticWriter<BeltInstance> {
	public static final BeltWriter INSTANCE = new BeltWriter();

	@Override
	public void write(long ptr, BeltInstance d) {
		super.write(ptr, d);
        MemoryUtil.memPutFloat(ptr + 26, d.qX);
		MemoryUtil.memPutFloat(ptr + 30, d.qY);
		MemoryUtil.memPutFloat(ptr + 34, d.qZ);
		MemoryUtil.memPutFloat(ptr + 38, d.qW);
		MemoryUtil.memPutFloat(ptr + 42, d.sourceU);
		MemoryUtil.memPutFloat(ptr + 46, d.sourceV);
		MemoryUtil.memPutFloat(ptr + 50, d.minU);
		MemoryUtil.memPutFloat(ptr + 54, d.minV);
		MemoryUtil.memPutFloat(ptr + 58, d.maxU);
		MemoryUtil.memPutFloat(ptr + 62, d.maxV);
		MemoryUtil.memPutByte(ptr + 66, d.scrollMult);
	}
}
