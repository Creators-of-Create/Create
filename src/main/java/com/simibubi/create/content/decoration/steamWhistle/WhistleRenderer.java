package com.simibubi.create.content.decoration.steamWhistle;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.ponder.utility.WorldTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class WhistleRenderer extends SafeBlockEntityRenderer<WhistleBlockEntity> {

	public WhistleRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(WhistleBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = be.getBlockState();
		if (!(blockState.getBlock() instanceof WhistleBlock))
			return;

		Direction direction = blockState.getValue(WhistleBlock.FACING);
		WhistleSize size = blockState.getValue(WhistleBlock.SIZE);

		PartialModel mouth = size == WhistleSize.LARGE ? AllPartialModels.WHISTLE_MOUTH_LARGE
			: size == WhistleSize.MEDIUM ? AllPartialModels.WHISTLE_MOUTH_MEDIUM : AllPartialModels.WHISTLE_MOUTH_SMALL;

		float offset = be.animation.getValue(partialTicks);
		if (be.animation.getChaseTarget() > 0 && be.animation.getValue() > 0.5f) {
			float wiggleProgress = (WorldTickHolder.getTicks(be.getLevel()) + partialTicks) / 8f;
			offset -= Math.sin(wiggleProgress * (2 * Mth.PI) * (4 - size.ordinal())) / 16f;
		}

		CachedBuffers.partial(mouth, blockState)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(direction))
			.unCentre()
			.translate(0, offset * 4 / 16f, 0)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
