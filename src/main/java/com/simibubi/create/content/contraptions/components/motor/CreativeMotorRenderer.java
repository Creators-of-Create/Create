package com.simibubi.create.content.contraptions.components.motor;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

public class CreativeMotorRenderer extends KineticTileEntityRenderer {

	public CreativeMotorRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return PartialBufferer.getFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState());
	}

}
