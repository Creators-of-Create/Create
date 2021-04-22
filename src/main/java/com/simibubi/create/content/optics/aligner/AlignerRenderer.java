package com.simibubi.create.content.optics.aligner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class AlignerRenderer extends SafeTileEntityRenderer<AlignerTileEntity> {
	public AlignerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(AlignerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		te.getHandler()
				.getRenderBeams()
				.forEachRemaining(beam -> beam.render(ms, buffer, partialTicks));
	}
}
