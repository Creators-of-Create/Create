package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;

public abstract class MappedBuffer implements AutoCloseable {

	protected boolean mapped;
	protected final GlBuffer owner;
	protected ByteBuffer internal;

	public MappedBuffer(GlBuffer owner) {
		this.owner = owner;
	}

	public void setInternal(ByteBuffer internal) {
		this.internal = internal;
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
		internal.asFloatBuffer().put(floats);
		internal.position(internal.position() + floats.length * 4);

		return this;
	}

	public MappedBuffer putByteArray(byte[] bytes) {
		checkAndMap();
		internal.put(bytes);

		return this;
	}

	/**
	 * Position this buffer relative to the 0-index in GPU memory.
	 *
	 * @return This buffer.
	 */
	public MappedBuffer position(int p) {
		checkAndMap();
		internal.position(p);
		return this;
	}

	public MappedBuffer putFloat(float f) {
		checkAndMap();
		internal.putFloat(f);
		return this;
	}

	public MappedBuffer putInt(int i) {
		checkAndMap();
		internal.putInt(i);
		return this;
	}

	public MappedBuffer put(byte b) {
		checkAndMap();
		internal.put(b);
		return this;
	}

	public MappedBuffer putVec4(float x, float y, float z, float w) {
		checkAndMap();
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);
		internal.putFloat(w);

		return this;
	}

	public MappedBuffer putVec3(float x, float y, float z) {
		checkAndMap();
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);

		return this;
	}

	public MappedBuffer putVec2(float x, float y) {
		checkAndMap();
		internal.putFloat(x);
		internal.putFloat(y);

		return this;
	}

	public MappedBuffer putVec3(byte x, byte y, byte z) {
		checkAndMap();
		internal.put(x);
		internal.put(y);
		internal.put(z);

		return this;
	}

	public MappedBuffer putVec2(byte x, byte y) {
		checkAndMap();
		internal.put(x);
		internal.put(y);

		return this;
	}
}
