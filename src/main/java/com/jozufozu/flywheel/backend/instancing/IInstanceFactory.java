package com.jozufozu.flywheel.backend.instancing;

public interface IInstanceFactory<D extends InstanceData> {
	D create(Instancer<? super D> owner);
}
