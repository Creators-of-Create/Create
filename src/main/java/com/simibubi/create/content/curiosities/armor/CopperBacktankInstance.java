package com.simibubi.create.content.curiosities.armor;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

public class CopperBacktankInstance extends SingleRotatingInstance {

	public CopperBacktankInstance(MaterialManager modelManager, KineticTileEntity tile) {
		super(modelManager, tile);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(AllBlockPartials.COPPER_BACKTANK_SHAFT, blockState);
	}

}
