package com.simibubi.create.content.contraptions.components.steam.whistle;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleBlock.WhistleSize;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class WhistleRenderer extends SafeTileEntityRenderer<WhistleTileEntity> {

	public WhistleRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(WhistleTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		if (!(blockState.getBlock() instanceof WhistleBlock))
			return;

		Direction direction = blockState.getValue(WhistleBlock.FACING);
		WhistleSize size = blockState.getValue(WhistleBlock.SIZE);

		PartialModel mouth = size == WhistleSize.LARGE ? AllBlockPartials.WHISTLE_MOUTH_LARGE
			: size == WhistleSize.MEDIUM ? AllBlockPartials.WHISTLE_MOUTH_MEDIUM : AllBlockPartials.WHISTLE_MOUTH_SMALL;

		float offset = te.animation.getValue(partialTicks);
		if (te.animation.getChaseTarget() > 0 && te.animation.getValue() > 0.5f) {
			float wiggleProgress = (AnimationTickHolder.getTicks(te.getLevel()) + partialTicks) / 8f;
			offset -= Math.sin(wiggleProgress * (2 * Mth.PI) * (4 - size.ordinal())) / 16f;
		}

		CachedBufferer.partial(mouth, blockState)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(direction))
			.unCentre()
			.translate(0, offset * 4 / 16f, 0)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
