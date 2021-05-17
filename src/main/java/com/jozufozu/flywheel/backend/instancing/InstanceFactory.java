package com.jozufozu.flywheel.backend.instancing;

public interface InstanceFactory<D extends InstanceData> {
	D create(InstancedModel<? super D> owner);
}
