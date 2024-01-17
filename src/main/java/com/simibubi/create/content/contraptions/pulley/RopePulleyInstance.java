package com.simibubi.create.content.contraptions.pulley;


import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.render.VirtualRenderHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class RopePulleyInstance extends AbstractPulleyInstance<PulleyBlockEntity> {
	public RopePulleyInstance(VisualizationContext materialManager, PulleyBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	protected Instancer<OrientedInstance> getRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, VirtualRenderHelper.blockModel(AllBlocks.ROPE.getDefaultState()), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, VirtualRenderHelper.blockModel(AllBlocks.PULLEY_MAGNET.getDefaultState()), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getHalfMagnetModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.ROPE_HALF_MAGNET), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getCoilModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.ROPE_COIL, rotatingAbout), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Instancer<OrientedInstance> getHalfRopeModel() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.ROPE_HALF), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected float getOffset() {
		float partialTicks = AnimationTickHolder.getPartialTicks();
		return PulleyRenderer.getBlockEntityOffset(partialTicks, blockEntity);
	}

	protected boolean isRunning() {
		return PulleyRenderer.isPulleyRunning(blockEntity);
	}
}
