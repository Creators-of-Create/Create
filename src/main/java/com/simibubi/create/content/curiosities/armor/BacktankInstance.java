package com.simibubi.create.content.curiosities.armor;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

public class BacktankInstance extends SingleRotatingInstance {

	public BacktankInstance(MaterialManager modelManager, KineticTileEntity tile) {
		super(modelManager, tile);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(BacktankRenderer.getShaftModel(blockState), blockState);
	}

}
