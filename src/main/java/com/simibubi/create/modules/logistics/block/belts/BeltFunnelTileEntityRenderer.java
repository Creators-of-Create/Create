package com.simibubi.create.modules.logistics.block.belts;

import com.simibubi.create.modules.logistics.block.FilteredTileEntityRenderer;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class BeltFunnelTileEntityRenderer extends TileEntityRenderer<BeltFunnelTileEntity> {

	FilteredTileEntityRenderer filterRenderer;

	public BeltFunnelTileEntityRenderer() {
		filterRenderer = new FilteredTileEntityRenderer();
	}

	@Override
	public void render(BeltFunnelTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {
		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
		filterRenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
	}

}
