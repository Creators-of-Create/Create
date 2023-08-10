package com.simibubi.create.content.contraptions.pulley;


import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import net.createmod.catnip.utility.AnimationTickHolder;

public class RopePulleyInstance extends AbstractPulleyInstance<PulleyBlockEntity> {
	public RopePulleyInstance(MaterialManager materialManager, PulleyBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	protected Instancer<OrientedData> getRopeModel() {
		return getOrientedMaterial().getModel(AllBlocks.ROPE.getDefaultState());
	}

	protected Instancer<OrientedData> getMagnetModel() {
		return getOrientedMaterial().getModel(AllBlocks.PULLEY_MAGNET.getDefaultState());
	}

	protected Instancer<OrientedData> getHalfMagnetModel() {
		return getOrientedMaterial().getModel(AllPartialModels.ROPE_HALF_MAGNET, blockState);
	}

	protected Instancer<OrientedData> getCoilModel() {
		return getOrientedMaterial().getModel(AllPartialModels.ROPE_COIL, blockState, rotatingAbout);
	}

	protected Instancer<OrientedData> getHalfRopeModel() {
		return getOrientedMaterial().getModel(AllPartialModels.ROPE_HALF, blockState);
	}

	protected float getOffset() {
		float partialTicks = AnimationTickHolder.getPartialTicks();
		return PulleyRenderer.getBlockEntityOffset(partialTicks, blockEntity);
	}

	protected boolean isRunning() {
		return PulleyRenderer.isPulleyRunning(blockEntity);
	}
}
