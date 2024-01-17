package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

public class ShaftInstance<T extends KineticBlockEntity> extends SingleRotatingInstance<T> {

	public ShaftInstance(VisualizationContext materialManager, T blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Model model() {
		return VirtualRenderHelper.blockModel(shaft());
	}
}
