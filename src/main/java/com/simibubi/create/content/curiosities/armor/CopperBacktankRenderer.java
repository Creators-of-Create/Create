package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class CopperBacktankRenderer extends KineticTileEntityRenderer {

	public CopperBacktankRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return PartialBufferer.get(AllBlockPartials.COPPER_BACKTANK_SHAFT, te.getBlockState());
	}

}
