package com.simibubi.create.content.logistics.block.belts.observer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;

public class BeltObserverTileEntityRenderer extends SafeTileEntityRenderer<BeltObserverTileEntity> {

	public BeltObserverTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BeltObserverTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		light = WorldRenderer.getLightmapCoordinates(te.getWorld(),
				te.getPos().up().offset(te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING)));

		FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);
	}

}
