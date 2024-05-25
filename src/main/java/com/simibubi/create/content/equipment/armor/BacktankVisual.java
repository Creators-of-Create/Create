package com.simibubi.create.content.equipment.armor;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
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
