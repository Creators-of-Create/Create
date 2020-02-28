package com.simibubi.create.modules.contraptions.relays.advanced;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class SpeedControllerRenderer extends SmartTileEntityRenderer<SpeedControllerTileEntity> {

	@Override
	public void renderWithGL(SpeedControllerTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {
		super.renderWithGL(tileEntityIn, x, y, z, partialTicks, destroyStage);

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		KineticTileEntityRenderer.renderRotatingBuffer(tileEntityIn, getWorld(), getRotatedModel(tileEntityIn), x, y, z,
				Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();
	}

	private SuperByteBuffer getRotatedModel(SpeedControllerTileEntity te) {
		BlockState state = te.getBlockState();
		return CreateClient.bufferCache.renderBlockIn(KineticTileEntityRenderer.KINETIC_TILE,
				AllBlocks.SHAFT.getDefault().with(ShaftBlock.AXIS, state.get(SpeedControllerBlock.HORIZONTAL_AXIS)));
	}

}
