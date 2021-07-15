package com.simibubi.create.content.curiosities.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class CopperBacktankRenderer extends KineticTileEntityRenderer {

	public CopperBacktankRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		SuperByteBuffer cogs =
				CreateClient.BUFFER_CACHE.renderPartial(AllBlockPartials.COPPER_BACKTANK_COGS, te.getBlockState());
		cogs.matrixStacker()
			.centre()
			.rotateY(180 + AngleHelper.horizontalAngle(te.getBlockState()
				.getValue(CopperBacktankBlock.HORIZONTAL_FACING)))
			.unCentre()
			.translate(0, 6.5f / 16, 11f / 16)
			.rotate(Direction.EAST,
				AngleHelper.rad(te.getSpeed() / 4f * AnimationTickHolder.getRenderTime(te.getLevel()) % 360))
			.translate(0, -6.5f / 16, -11f / 16);
		cogs.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return PartialBufferer.get(AllBlockPartials.COPPER_BACKTANK_SHAFT, te.getBlockState());
	}

}
