package com.simibubi.create.content.equipment.armor;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;

public class BacktankInstance extends SingleRotatingInstance<BacktankBlockEntity> {

	public BacktankInstance(VisualizationContext materialManager, BacktankBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Model model() {
		return Models.partial(BacktankRenderer.getShaftModel(blockState));
	}
}
