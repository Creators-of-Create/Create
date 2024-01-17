package com.simibubi.create.content.equipment.armor;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

public class BacktankVisual extends SingleRotatingVisual<BacktankBlockEntity> {

	public BacktankVisual(VisualizationContext context, BacktankBlockEntity blockEntity) {
		super(context, blockEntity);
	}

	@Override
	protected Model model() {
		return Models.partial(BacktankRenderer.getShaftModel(blockState));
	}
}
