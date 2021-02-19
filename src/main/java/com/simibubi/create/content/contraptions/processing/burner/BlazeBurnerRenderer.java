package com.simibubi.create.content.contraptions.processing.burner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class BlazeBurnerRenderer extends SafeTileEntityRenderer<BlazeBurnerTileEntity> {

	public BlazeBurnerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BlazeBurnerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		HeatLevel heatLevel = te.getHeatLevelFromBlock();
		if (heatLevel == HeatLevel.NONE)
			return;

		float renderTick = AnimationTickHolder.getRenderTick() + (te.hashCode() % 13) * 16f;
		float offset = (MathHelper.sin((float) ((renderTick / 16f) % (2 * Math.PI))) + .5f) / 16f;

		AllBlockPartials blazeModel = AllBlockPartials.BLAZES.get(heatLevel);
		SuperByteBuffer blazeBuffer = blazeModel.renderOn(te.getBlockState());
		blazeBuffer.rotateCentered(Direction.UP, AngleHelper.rad(te.headAngle.getValue(partialTicks)));
		blazeBuffer.translate(0, offset, 0);
		blazeBuffer.light(0xF000F0)
			.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}
}
