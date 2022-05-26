package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyTileEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class HosePulleyInstance extends AbstractPulleyInstance {

	public HosePulleyInstance(MaterialManager dispatcher, HosePulleyTileEntity tile) {
		super(dispatcher, tile);
	}

	protected Instancer<OrientedData> getRopeModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE);
	}

	protected Instancer<OrientedData> getMagnetModel() {
		return materialManager.defaultCutout()
				.material(Materials.ORIENTED)
				.getModel(AllBlockPartials.HOSE_MAGNET);
	}

	protected Instancer<OrientedData> getHalfMagnetModel() {
		return materialManager.defaultCutout()
				.material(Materials.ORIENTED)
				.getModel(AllBlockPartials.HOSE_HALF_MAGNET);
	}

	protected Instancer<OrientedData> getCoilModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE_COIL, rotatingAbout);
	}

	protected Instancer<OrientedData> getHalfRopeModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE_HALF);
	}

	protected float getOffset() {
		return ((HosePulleyTileEntity) blockEntity).getInterpolatedOffset(AnimationTickHolder.getPartialTicks());
	}

	protected boolean isRunning() {
		return true;
	}
}
