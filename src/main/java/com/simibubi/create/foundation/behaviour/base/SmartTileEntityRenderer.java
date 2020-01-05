package com.simibubi.create.foundation.behaviour.base;

import com.simibubi.create.foundation.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.behaviour.linked.LinkRenderer;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class SmartTileEntityRenderer<T extends SmartTileEntity> extends TileEntityRenderer<T> {

	@Override
	public void render(T tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
		FilteringRenderer.renderOnTileEntity(tileEntityIn, x, y, z, partialTicks, destroyStage);
		LinkRenderer.renderOnTileEntity(tileEntityIn, x, y, z, partialTicks, destroyStage);
	}

}
