package com.simibubi.create.content.curiosities.projector;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.TileEntityInstance;

public class ChromaticProjectorInstance extends TileEntityInstance<ChromaticProjectorTileEntity> implements IDynamicInstance {

	public ChromaticProjectorInstance(InstancedTileRenderer<?> renderer, ChromaticProjectorTileEntity tile) {
		super(renderer, tile);
	}

	@Override
	public void beginFrame() {
		Backend.effects.addSphere(tile.getFilter());
	}

	@Override
	public boolean decreaseFramerateWithDistance() {
		return false;
	}

	@Override
	public void remove() {

	}
}
