package com.simibubi.create.foundation.behaviour.base;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class SmartTileEntityRenderer<T extends SmartTileEntity> extends SafeTileEntityRenderer<T> {

	public SmartTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	protected void renderSafe(T tileEntityIn, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light,
			int overlay) {
		FilteringRenderer.renderOnTileEntity(tileEntityIn, partialTicks, ms, buffer, light, overlay);
		LinkRenderer.renderOnTileEntity(tileEntityIn, partialTicks, ms, buffer, light, overlay);
	}

}
