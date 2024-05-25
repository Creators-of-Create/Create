package com.simibubi.create.content.contraptions.pulley;

import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;

public class HosePulleyVisual extends AbstractPulleyVisual<HosePulleyBlockEntity> {

	public HosePulleyVisual(VisualizationContext dispatcher, HosePulleyBlockEntity blockEntity) {
		super(dispatcher, blockEntity);
	}

	protected Instancer<OrientedInstance> getRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE));
	}

	protected Instancer<OrientedInstance> getMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_MAGNET));
	}

	protected Instancer<OrientedInstance> getHalfMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_HALF_MAGNET));
	}

	protected Instancer<OrientedInstance> getCoilModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_COIL, rotatingAbout));
	}

	protected Instancer<OrientedInstance> getHalfRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_HALF));
	}

	protected float getOffset(float pt) {
		return blockEntity.getInterpolatedOffset(pt);
	}

	protected boolean isRunning() {
		return true;
	}
}
