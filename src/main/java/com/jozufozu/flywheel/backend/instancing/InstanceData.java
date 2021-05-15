package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.gl.MappedBuffer;

public abstract class InstanceData {

	protected final InstancedModel<?> owner;

	boolean dirty;
	boolean removed;

	protected InstanceData(InstancedModel<?> owner) {
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
