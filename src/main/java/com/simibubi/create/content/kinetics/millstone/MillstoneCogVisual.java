package com.simibubi.create.content.kinetics.millstone;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

public class MillstoneCogVisual extends SingleRotatingVisual<MillstoneBlockEntity> {

    public MillstoneCogVisual(VisualizationContext context, MillstoneBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

	@Override
	protected Model model() {
		return Models.partial(AllPartialModels.MILLSTONE_COG);
	}
}
