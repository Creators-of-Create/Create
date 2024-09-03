package com.simibubi.create.content.contraptions.pulley;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;

import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;

public class HosePulleyVisual extends AbstractPulleyVisual<HosePulleyBlockEntity> {
	public HosePulleyVisual(VisualizationContext dispatcher, HosePulleyBlockEntity blockEntity, float partialTick) {
		super(dispatcher, blockEntity, partialTick);
	}

	@Override
	protected Instancer<OrientedInstance> getRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE));
	}

	@Override
	protected Instancer<OrientedInstance> getMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_MAGNET));
	}

	@Override
	protected Instancer<OrientedInstance> getHalfMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_HALF_MAGNET));
	}

	@Override
	protected Instancer<OrientedInstance> getCoilModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_COIL, rotatingAbout));
	}

	@Override
	protected Instancer<OrientedInstance> getHalfRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_HALF));
	}

	@Override
	protected float getOffset(float pt) {
		return blockEntity.getInterpolatedOffset(pt);
	}

	@Override
	protected boolean isRunning() {
		return true;
	}
}
