package com.simibubi.create.content.contraptions.components.structureMovement.pulley;


import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class RopePulleyInstance extends AbstractPulleyInstance {
	final PulleyTileEntity tile = (PulleyTileEntity) super.tile;

	public RopePulleyInstance(MaterialManager dispatcher, PulleyTileEntity tile) {
		super(dispatcher, tile);
		beginFrame();
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
		return PulleyRenderer.getTileOffset(partialTicks, tile);
	}

	protected boolean isRunning() {
		return tile.running || tile.isVirtual();
	}
}
