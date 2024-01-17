package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;

public class CutoutRotatingVisual<T extends KineticBlockEntity> extends SingleRotatingVisual<T> {
	public CutoutRotatingVisual(VisualizationContext context, T blockEntity) {
		super(context, blockEntity);
	}
}
