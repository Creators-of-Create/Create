package com.simibubi.create.foundation.behaviour.base;

import com.simibubi.create.foundation.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;

public class SmartTileEntityRenderer<T extends SmartTileEntity> extends SafeTileEntityRenderer<T> {

	@Override
	public void renderWithGL(T tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
		FilteringRenderer.renderOnTileEntity(tileEntityIn, x, y, z, partialTicks, destroyStage);
		LinkRenderer.renderOnTileEntity(tileEntityIn, x, y, z, partialTicks, destroyStage);
	}

}
