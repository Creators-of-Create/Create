package com.simibubi.create.content.optics;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LightHandlerRenderer<T extends TileEntity & ILightHandler.ILightHandlerProvider> extends SafeTileEntityRenderer<T> {
	public LightHandlerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(T te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		te.getHandler()
				.getRenderBeams()
				.forEachRemaining(beam -> beam.render(ms, buffer, partialTicks));
	}

	@Override
	public boolean isGlobalRenderer(T te) {
		return true;
	}
}
