package com.simibubi.create.content.contraptions.components.motor;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.render.InstancedBuffer;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class CreativeMotorRenderer extends KineticTileEntityRenderer {

	public CreativeMotorRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected InstancedBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthInstanced(te.getBlockState());
	}

}
