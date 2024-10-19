package com.simibubi.create.content.trains.bogey;

import com.simibubi.create.content.trains.entity.CarriageBogey;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;

@FunctionalInterface
public interface BogeyVisualizer {
	BogeyVisual createVisual(VisualizationContext ctx, CarriageBogey bogey, float partialTick);
}
