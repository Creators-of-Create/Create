package com.simibubi.create.content.curiosities.projector;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.TileEntityInstance;
import com.simibubi.create.foundation.render.effects.EffectsHandler;

public class ChromaticProjectorInstance extends TileEntityInstance<ChromaticProjectorTileEntity> implements IDynamicInstance {

	public ChromaticProjectorInstance(MaterialManager<?> renderer, ChromaticProjectorTileEntity tile) {
		super(renderer, tile);
	}

	@Override
	public void beginFrame() {
		EffectsHandler.getInstance().addSphere(tile.getFilter());
	}

	@Override
	public boolean decreaseFramerateWithDistance() {
		return false;
	}

	@Override
	public void remove() {

	}
}
