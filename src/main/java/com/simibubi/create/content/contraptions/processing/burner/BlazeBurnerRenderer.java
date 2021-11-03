package com.simibubi.create.content.contraptions.processing.burner;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class BlazeBurnerRenderer extends SafeTileEntityRenderer<BlazeBurnerTileEntity> {

	public BlazeBurnerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(BlazeBurnerTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		HeatLevel heatLevel = te.getHeatLevelFromBlock();
		if (heatLevel == HeatLevel.NONE)
			return;

		float renderTick = AnimationTickHolder.getRenderTime(te.getLevel()) + (te.hashCode() % 13) * 16f;
		float offset = (Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) + .5f) / 16f;

		PartialModel blazeModel = AllBlockPartials.BLAZES.get(heatLevel);
		SuperByteBuffer blazeBuffer = PartialBufferer.get(blazeModel, te.getBlockState());
		blazeBuffer.rotateCentered(Direction.UP, AngleHelper.rad(te.headAngle.getValue(partialTicks)));
		blazeBuffer.translate(0, offset, 0);
		blazeBuffer.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}
}
