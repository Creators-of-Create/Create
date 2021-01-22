package com.simibubi.create.content.logistics.block.chute;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class SmartChuteRenderer extends SmartTileEntityRenderer<SmartChuteTileEntity> {

	public SmartChuteRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	protected void renderSafe(SmartChuteTileEntity tileEntityIn, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderSafe(tileEntityIn, partialTicks, ms, buffer, light, overlay);
		if (tileEntityIn.item.isEmpty())
			return;
		if (tileEntityIn.itemPosition.get(partialTicks) > 0)
			return;
		ChuteRenderer.renderItem(tileEntityIn, partialTicks, ms, buffer, light, overlay);
	}

}
