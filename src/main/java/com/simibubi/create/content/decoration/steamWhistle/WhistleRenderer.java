package com.simibubi.create.content.decoration.steamWhistle;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

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
			float wiggleProgress = (AnimationTickHolder.getTicks(be.getLevel()) + partialTicks) / 8f;
			offset -= Math.sin(wiggleProgress * (2 * Mth.PI) * (4 - size.ordinal())) / 16f;
		}

		CachedBufferer.partial(mouth, blockState)
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(direction))
			.uncenter()
			.translate(0, offset * 4 / 16f, 0)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
