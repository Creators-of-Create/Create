package com.simibubi.create.content.curiosities.armor;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

public class BacktankInstance extends SingleRotatingInstance {

	public BacktankInstance(MaterialManager materialManager, KineticBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(BacktankRenderer.getShaftModel(blockState), blockState);
	}

}
