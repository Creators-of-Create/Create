package com.simibubi.create.content.trains.bogey;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;

@FunctionalInterface
public interface BogeyVisualizer {
	BogeyVisual createVisual(VisualizationContext ctx, float partialTick, boolean inContraption);
}
