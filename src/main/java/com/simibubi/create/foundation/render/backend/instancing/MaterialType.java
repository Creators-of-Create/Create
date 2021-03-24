package com.simibubi.create.foundation.render.backend.instancing;

import com.simibubi.create.foundation.render.backend.gl.BasicProgram;

public class MaterialType<M extends InstancedModel<?>> {

	public <P extends BasicProgram> RenderMaterial<?, M> get(InstancedTileRenderer<P> renderer) {
		return renderer.getMaterial(this);
	}
}
