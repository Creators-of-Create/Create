package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;

public class CutoutRotatingInstance<T extends KineticBlockEntity> extends SingleRotatingInstance<T> {
	public CutoutRotatingInstance(VisualizationContext materialManager, T blockEntity) {
		super(materialManager, blockEntity);
	}
}
