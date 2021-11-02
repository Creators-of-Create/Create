package com.simibubi.create.foundation.tileEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SmartTileEntityRenderer<T extends SmartTileEntity> extends SafeTileEntityRenderer<T> {

	public SmartTileEntityRenderer(BlockEntityRendererProvider.Context context) {
	}
	
	@Override
	protected void renderSafe(T tileEntityIn, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
		FilteringRenderer.renderOnTileEntity(tileEntityIn, partialTicks, ms, buffer, light, overlay);
		LinkRenderer.renderOnTileEntity(tileEntityIn, partialTicks, ms, buffer, light, overlay);
	}

}
