package com.simibubi.create.content.contraptions.elevator;

import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.light.TickingLightListener;
import com.simibubi.create.content.kinetics.base.ShaftInstance;

// TODO
public class ElevatorPulleyInstance extends ShaftInstance<ElevatorPulleyBlockEntity> implements DynamicVisual, TickingLightListener {

	public ElevatorPulleyInstance(VisualizationContext materialManager, ElevatorPulleyBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	public boolean tickLightListener() {
		return false;
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
	}

}
