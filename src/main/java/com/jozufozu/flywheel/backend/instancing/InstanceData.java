package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;

public abstract class InstanceData {

	protected final Instancer<?> owner;

	boolean dirty;
	boolean removed;

	protected InstanceData(Instancer<?> owner) {
		this.owner = owner;
	}

	public abstract void write(MappedBuffer buf);

	public void markDirty() {
		owner.anyToUpdate = true;
		dirty = true;
	}

	public void delete() {
		owner.anyToRemove = true;
		removed = true;
	}

}
