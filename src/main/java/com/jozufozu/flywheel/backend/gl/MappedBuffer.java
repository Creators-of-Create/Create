package com.jozufozu.flywheel.backend.gl;

import java.nio.ByteBuffer;

public abstract class MappedBuffer implements AutoCloseable {

	private final ByteBuffer internal;

	public MappedBuffer(ByteBuffer internal) {
		this.internal = internal;
	}

	public MappedBuffer putFloatArray(float[] floats) {
		internal.asFloatBuffer().put(floats);
		internal.position(internal.position() + floats.length * 4);

		return this;
	}

	public MappedBuffer putByteArray(byte[] bytes) {
		internal.put(bytes);

		return this;
	}

	public MappedBuffer position(int p) {
		internal.position(p);
		return this;
	}

	public MappedBuffer putFloat(float f) {
		internal.putFloat(f);
		return this;
	}

	public MappedBuffer putInt(int i) {
		internal.putInt(i);
		return this;
	}

	public MappedBuffer put(byte b) {
		internal.put(b);
		return this;
	}

	public MappedBuffer putVec4(float x, float y, float z, float w) {
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);
		internal.putFloat(w);

		return this;
	}

	public MappedBuffer putVec3(float x, float y, float z) {
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);

		return this;
	}

	public MappedBuffer putVec2(float x, float y) {
		internal.putFloat(x);
		internal.putFloat(y);

		return this;
	}

	public MappedBuffer putVec3(byte x, byte y, byte z) {
		internal.put(x);
		internal.put(y);
		internal.put(z);

		return this;
	}

	public MappedBuffer putVec2(byte x, byte y) {
		internal.put(x);
		internal.put(y);

		return this;
	}
}
