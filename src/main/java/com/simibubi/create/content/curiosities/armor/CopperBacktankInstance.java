package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

public class CopperBacktankInstance extends SingleRotatingInstance {

	public CopperBacktankInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
		super(modelManager, tile);
	}

	@Override
	protected InstancedModel<RotatingData> getModel() {
		return getRotatingMaterial().getModel(AllBlockPartials.COPPER_BACKTANK_SHAFT, blockState);
	}

}
