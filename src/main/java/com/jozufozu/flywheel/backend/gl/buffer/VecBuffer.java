package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VecBuffer {

	protected ByteBuffer internal;

	public VecBuffer() {
	}

	public VecBuffer(ByteBuffer internal) {
		this.internal = internal;
	}

	public static VecBuffer allocate(int bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(bytes);
		buffer.order(ByteOrder.nativeOrder());
		return new VecBuffer(buffer);
	}

	protected void setInternal(ByteBuffer internal) {
		this.internal = internal;
	}

	public ByteBuffer unwrap() {
		return internal;
	}

	public VecBuffer rewind() {
		internal.rewind();

		return this;
	}

	public VecBuffer putFloatArray(float[] floats) {

		for (float f : floats) {
			internal.putFloat(f);
		}
//		internal.asFloatBuffer().put(floats);
//		internal.position(internal.position() + floats.length * 4);

		return this;
	}

	public VecBuffer putByteArray(byte[] bytes) {
		internal.put(bytes);
		return this;
	}

	/**
	 * Position this buffer relative to the 0-index in GPU memory.
	 *
	 * @return This buffer.
	 */
	public VecBuffer position(int p) {
		internal.position(p);
		return this;
	}

	public VecBuffer putFloat(float f) {
		internal.putFloat(f);
		return this;
	}

	public VecBuffer putInt(int i) {
		internal.putInt(i);
		return this;
	}

	public VecBuffer put(byte b) {
		internal.put(b);
		return this;
	}

	public VecBuffer put(ByteBuffer b) {
		internal.put(b);
		return this;
	}

	public VecBuffer putVec4(float x, float y, float z, float w) {
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);
		internal.putFloat(w);
		return this;
	}

	public VecBuffer putVec3(float x, float y, float z) {
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);
		return this;
	}

	public VecBuffer putVec2(float x, float y) {
		internal.putFloat(x);
		internal.putFloat(y);
		return this;
	}

	public VecBuffer putVec3(byte x, byte y, byte z) {
		internal.put(x);
		internal.put(y);
		internal.put(z);
		return this;
	}

	public VecBuffer putVec2(byte x, byte y) {
		internal.put(x);
		internal.put(y);
		return this;
	}
}
