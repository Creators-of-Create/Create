package com.simibubi.create.content.logistics.packagePort.postbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class PostboxRenderer extends SmartBlockEntityRenderer<PostboxBlockEntity> {

	public PostboxRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(PostboxBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {

		if (blockEntity.addressFilter != null && !blockEntity.addressFilter.isBlank()) {
            renderNameplateOnHover(blockEntity, Component.literal(blockEntity.addressFilter), 1, ms, buffer, light);
        }

		SuperByteBuffer sbb = CachedBuffers.partial(AllPartialModels.POSTBOX_FLAG, blockEntity.getBlockState());

		sbb.light(light)
			.overlay(overlay)
			.rotateCentered(Mth.DEG_TO_RAD * (180 - blockEntity.getBlockState()
				.getValue(PostboxBlock.FACING)
				.toYRot()), Axis.YP);

		transformFlag(sbb, blockEntity, partialTicks);

		sbb.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutout()));
	}

	public static void transformFlag(SuperByteBuffer flag, PostboxBlockEntity be, float partialTicks) {
		float value = be.flag.getValue(partialTicks);
		float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
		if (be.flag.getChaseTarget() > 0 && !be.flag.settled() && progress == 1) {
			float wiggleProgress = (value - .2f) / .8f;
			progress += (Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress);
		}
		flag.translate(0, 10 / 16f, 2 / 16f);
		flag.rotateXDegrees(-progress * 90);
		flag.translateBack(0, 10 / 16f, 2 / 16f);
	}

}
