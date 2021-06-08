package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;

public abstract class MappedBuffer extends VecBuffer implements AutoCloseable {

	protected boolean mapped;
	protected final GlBuffer owner;

	public MappedBuffer(GlBuffer owner) {
		this.owner = owner;
	}

	protected abstract void checkAndMap();

	/**
	 * Make the changes in client memory available to the GPU.
	 */
	public void flush() {
		if (mapped) {
			GL15.glUnmapBuffer(owner.type.glEnum);
			mapped = false;
			setInternal(null);
		}
	}

	@Override
	public void close() throws Exception {
		flush();
	}

	public MappedBuffer putFloatArray(float[] floats) {
		checkAndMap();
		super.putFloatArray(floats);
		return this;
	}

	public MappedBuffer putByteArray(byte[] bytes) {
		checkAndMap();
		super.putByteArray(bytes);
		return this;
	}

	/**
	 * Position this buffer relative to the 0-index in GPU memory.
	 *
	 * @return This buffer.
	 */
	public MappedBuffer position(int p) {
		checkAndMap();
		super.position(p);
		return this;
	}

	public MappedBuffer putFloat(float f) {
		checkAndMap();
		super.putFloat(f);
		return this;
	}

	public MappedBuffer putInt(int i) {
		checkAndMap();
		super.putInt(i);
		return this;
	}

	public MappedBuffer put(byte b) {
		checkAndMap();
		super.put(b);
		return this;
	}

	public MappedBuffer put(ByteBuffer b) {
		checkAndMap();
		super.put(b);
		return this;
	}

	public MappedBuffer putVec4(float x, float y, float z, float w) {
		checkAndMap();
		super.putVec4(x, y, z, w);
		return this;
	}

	public MappedBuffer putVec3(float x, float y, float z) {
		checkAndMap();
		super.putVec3(x, y, z);
		return this;
	}

	public MappedBuffer putVec2(float x, float y) {
		checkAndMap();
		super.putVec2(x, y);
		return this;
	}

	public MappedBuffer putVec3(byte x, byte y, byte z) {
		checkAndMap();
		super.putVec3(x, y, z);
		return this;
	}

	public MappedBuffer putVec2(byte x, byte y) {
		checkAndMap();
		super.putVec2(x, y);
		return this;
	}
}
