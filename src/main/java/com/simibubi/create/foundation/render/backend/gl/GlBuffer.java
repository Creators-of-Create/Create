package com.simibubi.create.foundation.render.backend.gl;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import com.simibubi.create.foundation.render.backend.Backend;

public class GlBuffer extends GlObject {

    protected final int bufferType;

	public GlBuffer(int bufferType) {
		setHandle(GL20.glGenBuffers());
		this.bufferType = bufferType;
	}

	public int getBufferType() {
		return bufferType;
	}

	public void bind() {
		bind(bufferType);
	}

	public void bind(int type) {
		GL20.glBindBuffer(type, handle());
	}

	public void unbind() {
		unbind(bufferType);
	}

	public void unbind(int bufferType) {
		GL20.glBindBuffer(bufferType, 0);
	}

	public void alloc(int size, int usage) {
		GL15.glBufferData(bufferType, size, usage);
	}

	public void with(Consumer<GlBuffer> action) {
		bind();
		action.accept(this);
		unbind();
	}

	public void map(int length, Consumer<ByteBuffer> upload) {
		Backend.compat.mapBuffer(bufferType, 0, length, upload);
	}

	public void map(int offset, int length, Consumer<ByteBuffer> upload) {
		Backend.compat.mapBuffer(bufferType, offset, length, upload);
	}

	public void map(int type, int offset, int length, Consumer<ByteBuffer> upload) {
		Backend.compat.mapBuffer(type, offset, length, upload);
	}

	protected void deleteInternal(int handle) {
		GL20.glDeleteBuffers(handle);
	}
}
