package com.simibubi.create.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.linked.LinkRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SmartBlockEntityRenderer<T extends SmartBlockEntity> extends SafeBlockEntityRenderer<T> {

	public SmartBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}
	
	@Override
	protected void renderSafe(T blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
		FilteringRenderer.renderOnBlockEntity(blockEntity, partialTicks, ms, buffer, light, overlay);
		LinkRenderer.renderOnBlockEntity(blockEntity, partialTicks, ms, buffer, light, overlay);
	}

}
