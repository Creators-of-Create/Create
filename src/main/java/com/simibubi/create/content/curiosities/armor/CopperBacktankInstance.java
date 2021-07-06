package com.simibubi.create.content.curiosities.armor;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

public class CopperBacktankInstance extends SingleRotatingInstance {

	public CopperBacktankInstance(MaterialManager<?> modelManager, KineticTileEntity tile) {
		super(modelManager, tile);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(AllBlockPartials.COPPER_BACKTANK_SHAFT, blockState);
	}

}
