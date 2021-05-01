package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.instancing.InstancedModel;

public class MaterialType<M extends InstancedModel<?>> {

	@Override
	public int hashCode() {
		return super.hashCode() * 31 * 493286711;
	}
}
