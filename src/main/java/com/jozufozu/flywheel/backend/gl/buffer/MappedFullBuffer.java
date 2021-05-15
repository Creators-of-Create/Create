package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15;

public class MappedFullBuffer extends MappedBuffer {

	MappedBufferUsage usage;

	public MappedFullBuffer(GlBuffer buffer, MappedBufferUsage usage) {
		super(buffer);
		this.usage = usage;
	}

	@Override
	protected void checkAndMap() {
		if (!mapped) {
			setInternal(GL15.glMapBuffer(owner.type.glEnum, usage.glEnum));
			mapped = true;
		}
	}
}
