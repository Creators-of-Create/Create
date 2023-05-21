package com.simibubi.create.content.contraptions.pulley;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class HosePulleyInstance extends AbstractPulleyInstance<HosePulleyBlockEntity> {

	public HosePulleyInstance(MaterialManager dispatcher, HosePulleyBlockEntity blockEntity) {
		super(dispatcher, blockEntity);
	}

	protected Instancer<OrientedData> getRopeModel() {
		return getOrientedMaterial().getModel(AllPartialModels.HOSE, blockState);
	}

	protected Instancer<OrientedData> getMagnetModel() {
		return materialManager.defaultCutout()
				.material(Materials.ORIENTED)
				.getModel(AllPartialModels.HOSE_MAGNET, blockState);
	}

	protected Instancer<OrientedData> getHalfMagnetModel() {
		return materialManager.defaultCutout()
				.material(Materials.ORIENTED)
				.getModel(AllPartialModels.HOSE_HALF_MAGNET, blockState);
	}

	protected Instancer<OrientedData> getCoilModel() {
		return getOrientedMaterial().getModel(AllPartialModels.HOSE_COIL, blockState, rotatingAbout);
	}

	protected Instancer<OrientedData> getHalfRopeModel() {
		return getOrientedMaterial().getModel(AllPartialModels.HOSE_HALF, blockState);
	}

	protected float getOffset() {
		return blockEntity.getInterpolatedOffset(AnimationTickHolder.getPartialTicks());
	}

	protected boolean isRunning() {
		return true;
	}
}
