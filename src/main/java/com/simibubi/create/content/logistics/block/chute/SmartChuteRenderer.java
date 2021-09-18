package com.simibubi.create.content.logistics.block.chute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SmartChuteRenderer extends SmartTileEntityRenderer<SmartChuteTileEntity> {

	public SmartChuteRenderer(BlockEntityRendererProvider.Context dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(SmartChuteTileEntity tileEntityIn, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(tileEntityIn, partialTicks, ms, buffer, light, overlay);
		if (tileEntityIn.item.isEmpty())
			return;
		if (tileEntityIn.itemPosition.get(partialTicks) > 0)
			return;
		ChuteRenderer.renderItem(tileEntityIn, partialTicks, ms, buffer, light, overlay);
	}

}
