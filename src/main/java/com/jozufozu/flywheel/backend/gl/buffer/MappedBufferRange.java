package com.jozufozu.flywheel.backend.gl.buffer;

import com.jozufozu.flywheel.backend.Backend;

public class MappedBufferRange extends MappedBuffer {

	long offset, length;
	int access;

	public MappedBufferRange(GlBuffer buffer, long offset, long length, int access) {
		super(buffer);
		this.offset = offset;
		this.length = length;
		this.access = access;
	}


	@Override
	public MappedBuffer position(int p) {
		if (p < offset || p >= offset + length) {
			throw new IndexOutOfBoundsException("Index " + p + " is not mapped");
		}
		return super.position(p - (int) offset);
	}

	@Override
	protected void checkAndMap() {
		if (!mapped) {
			setInternal(Backend.compat.mapBufferRange.mapBuffer(owner.type, offset, length, access));
			mapped = true;
		}
	}
}
