package com.simibubi.create.content.kinetics.base.flwdata;

import org.lwjgl.system.MemoryUtil;

public class RotatingWriter extends KineticWriter<RotatingInstance> {
	public static final RotatingWriter INSTANCE = new RotatingWriter();

	@Override
	public void write(long ptr, RotatingInstance d) {
		super.write(ptr, d);
        MemoryUtil.memPutByte(ptr + 26, d.rotationAxisX);
		MemoryUtil.memPutByte(ptr + 27, d.rotationAxisY);
		MemoryUtil.memPutByte(ptr + 28, d.rotationAxisZ);
	}
}
