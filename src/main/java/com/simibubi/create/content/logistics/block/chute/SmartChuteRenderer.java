package com.simibubi.create.content.logistics.block.chute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SmartChuteRenderer extends SmartBlockEntityRenderer<SmartChuteBlockEntity> {

	public SmartChuteRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(SmartChuteBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
		if (blockEntity.item.isEmpty())
			return;
		if (blockEntity.itemPosition.getValue(partialTicks) > 0)
			return;
		ChuteRenderer.renderItem(blockEntity, partialTicks, ms, buffer, light, overlay);
	}

}
