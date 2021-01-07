package com.simibubi.create.content.contraptions.components.millstone;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;

import com.simibubi.create.foundation.utility.render.instancing.RotatingBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class MillstoneRenderer extends KineticTileEntityRenderer {

	public MillstoneRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected RotatingBuffer getRotatedModel(KineticTileEntity te) {
		return CreateClient.kineticRenderer.renderPartialRotating(AllBlockPartials.MILLSTONE_COG, te.getBlockState());
	}
	
}
