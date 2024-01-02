package com.simibubi.create.content.kinetics.base.flwdata;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.instance.ColoredLitWriter;

public abstract class KineticWriter<D extends KineticInstance> extends ColoredLitWriter<D> {
	@Override
	public void write(long ptr, D d) {
		super.write(ptr, d);
        MemoryUtil.memPutFloat(ptr + 6, d.x);
		MemoryUtil.memPutFloat(ptr + 10, d.y);
		MemoryUtil.memPutFloat(ptr + 14, d.z);
		MemoryUtil.memPutFloat(ptr + 18, d.rotationalSpeed);
		MemoryUtil.memPutFloat(ptr + 22, d.rotationOffset);
	}
}
