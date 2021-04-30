package com.simibubi.create.foundation.render.backend.instancing;

import java.nio.ByteBuffer;

public abstract class InstanceData {

	protected final InstancedModel<?> owner;

	boolean dirty;
	boolean removed;

	protected InstanceData(InstancedModel<?> owner) {
		this.owner = owner;
	}

	public abstract void write(ByteBuffer buf);

	public void markDirty() {
		owner.anyToUpdate = true;
		dirty = true;
	}

	public void delete() {
		owner.anyToRemove = true;
		removed = true;
	}

	public void putVec4(ByteBuffer buf, float x, float y, float z, float w) {
		put(buf, x);
		put(buf, y);
		put(buf, z);
		put(buf, w);
	}

	public void putVec3(ByteBuffer buf, float x, float y, float z) {
		put(buf, x);
		put(buf, y);
		put(buf, z);
	}

	public void putVec2(ByteBuffer buf, float x, float y) {
		put(buf, x);
		put(buf, y);
	}

	public void putVec3(ByteBuffer buf, byte x, byte y, byte z) {
		put(buf, x);
		put(buf, y);
		put(buf, z);
	}

	public void putVec2(ByteBuffer buf, byte x, byte y) {
		put(buf, x);
		put(buf, y);
	}

	public void put(ByteBuffer buf, byte b) {
		buf.put(b);
	}

	public void put(ByteBuffer buf, float f) {
		buf.putFloat(f);
	}
}
