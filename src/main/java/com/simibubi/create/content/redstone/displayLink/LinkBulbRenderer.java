package com.simibubi.create.content.redstone.displayLink;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.util.LightCoordsUtil;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class LinkBulbRenderer extends SafeBlockEntityRenderer<LinkWithBulbBlockEntity> {

	public LinkBulbRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(LinkWithBulbBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		float glow = be.getGlow(partialTicks);
		if (glow < .125f)
			return;

		glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
		glow = Mth.clamp(glow, -1, 1);

		int color = (int) (200 * glow);

		BlockState blockState = be.getBlockState();
		var msr = TransformStack.of(ms);

		Direction face = be.getBulbFacing(blockState);

		ms.pushPose();

		msr.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(face) + 180)
			.rotateXDegrees(-AngleHelper.verticalAngle(face) - 90)
			.uncenter();

		CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_TUBE, blockState)
			.translate(be.getBulbOffset(blockState))
			.light(LightCoordsUtil.FULL_BRIGHT)
			.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.translucent()));

		CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_GLOW, blockState)
			.translate(be.getBulbOffset(blockState))
			.light(LightCoordsUtil.FULL_BRIGHT)
			.color(color, color, color, 255)
			.disableDiffuse()
			.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

		ms.popPose();
	}

}
