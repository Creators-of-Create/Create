package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyTileEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class HosePulleyInstance extends AbstractPulleyInstance {
	final HosePulleyTileEntity tile = (HosePulleyTileEntity) super.tile;

	public HosePulleyInstance(MaterialManager<?> dispatcher, HosePulleyTileEntity tile) {
		super(dispatcher, tile);
		beginFrame();
	}

	protected Instancer<OrientedData> getRopeModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE, blockState);
	}

	protected Instancer<OrientedData> getMagnetModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE_MAGNET, blockState);
	}

	protected Instancer<OrientedData> getHalfMagnetModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE_HALF_MAGNET, blockState);
	}

	protected Instancer<OrientedData> getCoilModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE_COIL, blockState, rotatingAbout);
	}

	protected Instancer<OrientedData> getHalfRopeModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.HOSE_HALF, blockState);
	}

	protected float getOffset() {
		return tile.getInterpolatedOffset(AnimationTickHolder.getPartialTicks());
	}

	protected boolean isRunning() {
		return true;
	}
}
