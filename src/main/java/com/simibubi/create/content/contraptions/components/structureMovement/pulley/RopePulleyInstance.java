package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import net.minecraft.world.ILightReader;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.render.backend.core.OrientedData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class RopePulleyInstance extends AbstractPulleyInstance {
	final PulleyTileEntity tile = (PulleyTileEntity) super.tile;

	public RopePulleyInstance(InstancedTileRenderer<?> dispatcher, PulleyTileEntity tile) {
		super(dispatcher, tile);
		beginFrame();
	}

	protected InstancedModel<OrientedData> getRopeModel() {
		return getOrientedMaterial().getModel(AllBlocks.ROPE.getDefaultState());
	}

	protected InstancedModel<OrientedData> getMagnetModel() {
		return getOrientedMaterial().getModel(AllBlocks.PULLEY_MAGNET.getDefaultState());
	}

	protected InstancedModel<OrientedData> getHalfMagnetModel() {
		return getOrientedMaterial().getModel(AllBlockPartials.ROPE_HALF_MAGNET, blockState);
	}

	protected InstancedModel<OrientedData> getCoilModel() {
		return AllBlockPartials.ROPE_COIL.getModel(getOrientedMaterial(), blockState, rotatingAbout);
	}

	protected InstancedModel<OrientedData> getHalfRopeModel() {
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
