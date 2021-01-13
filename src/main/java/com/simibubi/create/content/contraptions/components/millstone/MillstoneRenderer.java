package com.simibubi.create.content.contraptions.components.millstone;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;

import com.simibubi.create.foundation.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class MillstoneRenderer extends KineticTileEntityRenderer {

	public MillstoneRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected InstanceBuffer<RotatingData> getRotatedModel(InstanceContext<? extends KineticTileEntity> ctx) {
		return AllBlockPartials.MILLSTONE_COG.renderOnRotating(ctx, ctx.te.getBlockState());
	}
	
}
