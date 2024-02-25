package com.simibubi.create.content.contraptions.elevator;

import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.content.kinetics.base.ShaftVisual;

// TODO
public class ElevatorPulleyVisual extends ShaftVisual<ElevatorPulleyBlockEntity> implements SimpleDynamicVisual {

	public ElevatorPulleyVisual(VisualizationContext context, ElevatorPulleyBlockEntity blockEntity) {
		super(context, blockEntity);
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
	}

}
