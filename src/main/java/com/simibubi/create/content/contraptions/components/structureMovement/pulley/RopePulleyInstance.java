package com.simibubi.create.content.contraptions.components.structureMovement.pulley;


import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class RopePulleyInstance extends AbstractPulleyInstance {
	public RopePulleyInstance(MaterialManager dispatcher, PulleyTileEntity tile) {
		super(dispatcher, tile);
	}

	protected Instancer<OrientedData> getRopeModel() {
		return getOrientedMaterial().getModel(AllBlocks.ROPE.getDefaultState());
	}

	protected Instancer<OrientedData> getMagnetModel() {
		return getOrientedMaterial().getModel(AllBlocks.PULLEY_MAGNET.getDefaultState());
	}

	protected Instancer<OrientedData> getHalfMagnetModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.ROPE_HALF_MAGNET, blockState);
	}

	protected Instancer<OrientedData> getCoilModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.ROPE_COIL, blockState, rotatingAbout);
	}

	protected Instancer<OrientedData> getHalfRopeModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.ROPE_HALF, blockState);
	}

	protected float getOffset() {
		float partialTicks = AnimationTickHolder.getPartialTicks();
		return PulleyRenderer.getTileOffset(partialTicks, (PulleyTileEntity) blockEntity);
	}

	protected boolean isRunning() {
		return ((PulleyTileEntity) blockEntity).running || blockEntity.isVirtual();
	}
}
