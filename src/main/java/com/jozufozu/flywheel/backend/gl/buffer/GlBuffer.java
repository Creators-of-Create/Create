package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.MapBufferRange;

public class GlBuffer extends GlObject {

	protected final GlBufferType type;
	protected final GlBufferUsage usage;

	public GlBuffer(GlBufferType type) {
		this(type, GlBufferUsage.STATIC_DRAW);
	}

	public GlBuffer(GlBufferType type, GlBufferUsage usage) {
		setHandle(GL20.glGenBuffers());
		this.type = type;
		this.usage = usage;
	}

	public GlBufferType getBufferTarget() {
		return type;
	}

	public void bind() {
		bind(type);
	}

	public void bind(GlBufferType type) {
		GL20.glBindBuffer(type.glEnum, handle());
	}

	public void unbind() {
		unbind(type);
	}

	public void unbind(GlBufferType bufferType) {
		GL20.glBindBuffer(bufferType.glEnum, 0);
	}

	public void alloc(int size) {
		GL15.glBufferData(type.glEnum, size, usage.glEnum);
	}

	public MappedBuffer getBuffer(int offset, int length) {
		if (Backend.compat.mapBufferRange != MapBufferRange.UNSUPPORTED) {
			return new MappedBufferRange(this, offset, length, GL30.GL_MAP_WRITE_BIT);
		} else {
			MappedFullBuffer fullBuffer = new MappedFullBuffer(this, MappedBufferUsage.WRITE_ONLY);
			fullBuffer.position(offset);
			return fullBuffer;
		}
	}

	protected void deleteInternal(int handle) {
		GL20.glDeleteBuffers(handle);
	}
}
