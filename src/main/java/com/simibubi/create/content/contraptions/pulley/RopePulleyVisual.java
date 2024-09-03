package com.simibubi.create.content.contraptions.pulley;


import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;

public class RopePulleyVisual extends AbstractPulleyVisual<PulleyBlockEntity> {
	public RopePulleyVisual(VisualizationContext context, PulleyBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	@Override
	protected Instancer<OrientedInstance> getRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, VirtualRenderHelper.blockModel(AllBlocks.ROPE.getDefaultState()));
	}

	@Override
	protected Instancer<OrientedInstance> getMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, VirtualRenderHelper.blockModel(AllBlocks.PULLEY_MAGNET.getDefaultState()));
	}

	@Override
	protected Instancer<OrientedInstance> getHalfMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.ROPE_HALF_MAGNET));
	}

	@Override
	protected Instancer<OrientedInstance> getCoilModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.ROPE_COIL, rotatingAbout));
	}

	@Override
	protected Instancer<OrientedInstance> getHalfRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.ROPE_HALF));
	}

	@Override
	protected float getOffset(float pt) {
		return PulleyRenderer.getBlockEntityOffset(pt, blockEntity);
	}

	@Override
	protected boolean isRunning() {
		return PulleyRenderer.isPulleyRunning(blockEntity);
	}
}
