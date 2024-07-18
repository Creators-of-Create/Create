package com.simibubi.create.content.kinetics.base;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

public class ShaftVisual<T extends KineticBlockEntity> extends SingleRotatingVisual<T> {

	public ShaftVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	@Override
	protected Model model() {
		return VirtualRenderHelper.blockModel(shaft());
	}
}
