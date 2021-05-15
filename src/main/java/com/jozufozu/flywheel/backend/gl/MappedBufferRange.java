package com.jozufozu.flywheel.backend.gl;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL30;

public class MappedBufferRange extends MappedBuffer {

	GlBuffer owner;

	public MappedBufferRange(GlBuffer buffer, ByteBuffer internal) {
		super(internal);
		this.owner = buffer;
	}

	public static MappedBufferRange create(GlBuffer buffer, long offset, long length, int access) {
		ByteBuffer byteBuffer = GL30.glMapBufferRange(buffer.type.glEnum, offset, length, access);

		return new MappedBufferRange(buffer, byteBuffer);
	}

	public MappedBuffer unmap() {
		GL30.glUnmapBuffer(owner.type.glEnum);
		return this;
	}

	@Override
	public void close() throws Exception {
		unmap();
	}
}
