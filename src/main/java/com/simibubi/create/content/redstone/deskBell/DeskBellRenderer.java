package com.simibubi.create.content.redstone.deskBell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DeskBellRenderer extends SmartBlockEntityRenderer<DeskBellBlockEntity> {

	public DeskBellRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(DeskBellBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		BlockState blockState = blockEntity.getBlockState();
		float p = blockEntity.animation.getValue(partialTicks);
		if (p < 0.004 && !blockState.getOptionalValue(DeskBellBlock.POWERED)
			.orElse(false))
			return;

		float f = (float) (1 - 4 * Math.pow((Math.max(p - 0.5, 0)) - 0.5, 2));
		float f2 = (float) (Math.pow(p, 1.25f));

		Direction facing = blockState.getValue(DeskBellBlock.FACING);

		CachedBuffers.partial(AllPartialModels.DESK_BELL_PLUNGER, blockState)
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
			.uncenter()
			.translate(0, f * -.75f / 16f, 0)
			.light(light)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));

		CachedBuffers.partial(AllPartialModels.DESK_BELL_BELL, blockState)
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
			.translate(0, -1 / 16, 0)
			.rotateXDegrees(f2 * 8 * Mth.sin(p * Mth.PI * 4 + blockEntity.animationOffset))
			.rotateZDegrees(f2 * 8 * Mth.cos(p * Mth.PI * 4 + blockEntity.animationOffset))
			.translate(0, 1 / 16, 0)
			.scale(0.995f)
			.uncenter()
			.light(light)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));
	}

}
