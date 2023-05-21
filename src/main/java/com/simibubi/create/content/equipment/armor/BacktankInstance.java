package com.simibubi.create.content.equipment.armor;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;

public class BacktankInstance extends SingleRotatingInstance<BacktankBlockEntity> {

	public BacktankInstance(MaterialManager materialManager, BacktankBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(BacktankRenderer.getShaftModel(blockState), blockState);
	}

}
