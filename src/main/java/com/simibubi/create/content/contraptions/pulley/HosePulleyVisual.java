package com.simibubi.create.content.contraptions.pulley;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class HosePulleyVisual extends AbstractPulleyVisual<HosePulleyBlockEntity> {

	public HosePulleyVisual(VisualizationContext dispatcher, HosePulleyBlockEntity blockEntity) {
		super(dispatcher, blockEntity);
	}

	protected Instancer<OrientedInstance> getRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_MAGNET), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getHalfMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_HALF_MAGNET), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getCoilModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_COIL, rotatingAbout), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getHalfRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.HOSE_HALF), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected float getOffset() {
		return blockEntity.getInterpolatedOffset(AnimationTickHolder.getPartialTicks());
	}

	protected boolean isRunning() {
		return true;
	}
}
