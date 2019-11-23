package com.simibubi.create.modules.logistics.block.belts;

import com.simibubi.create.modules.logistics.block.FilteredTileEntityRenderer;
import com.simibubi.create.modules.logistics.block.LinkedTileEntityRenderer;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class LinkedExtractorTileEntityRenderer extends TileEntityRenderer<LinkedExtractorTileEntity> {

	LinkedTileEntityRenderer linkRenderer;

	public LinkedExtractorTileEntityRenderer() {
		linkRenderer = new LinkedTileEntityRenderer();
	}

	@Override
	public void render(LinkedExtractorTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {
		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
		linkRenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
		FilteredTileEntityRenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
	}

}
