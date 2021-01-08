package com.simibubi.create.content.contraptions.relays.advanced;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;

import com.simibubi.create.foundation.utility.render.instancing.IInstancedTileEntityRenderer;
import com.simibubi.create.foundation.utility.render.instancing.RotatingBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class SpeedControllerRenderer extends SmartTileEntityRenderer<SpeedControllerTileEntity> implements IInstancedTileEntityRenderer<SpeedControllerTileEntity> {

	public SpeedControllerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(SpeedControllerTileEntity tileEntityIn, float partialTicks, MatrixStack ms,
			IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderSafe(tileEntityIn, partialTicks, ms, buffer, light, overlay);

		addInstanceData(tileEntityIn);
	}

	@Override
	public void addInstanceData(SpeedControllerTileEntity te) {
		KineticTileEntityRenderer.renderRotatingBuffer(te, getRotatedModel(te));
	}

	private RotatingBuffer getRotatedModel(SpeedControllerTileEntity te) {
		return CreateClient.kineticRenderer.renderBlockInstanced(KineticTileEntityRenderer.KINETIC_TILE,
				KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te)));
	}

}
