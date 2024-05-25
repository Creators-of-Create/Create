package com.simibubi.create.content.contraptions.elevator;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.content.kinetics.base.ShaftVisual;

// TODO
public class ElevatorPulleyVisual extends ShaftVisual<ElevatorPulleyBlockEntity> implements SimpleDynamicVisual {

	public ElevatorPulleyVisual(VisualizationContext context, ElevatorPulleyBlockEntity blockEntity) {
		super(context, blockEntity);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
	}

}
