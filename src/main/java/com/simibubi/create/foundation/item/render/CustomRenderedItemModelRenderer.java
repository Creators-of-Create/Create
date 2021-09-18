package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRenderedItemModelRenderer<M extends CustomRenderedItemModel> extends BlockEntityWithoutLevelRenderer {

	public CustomRenderedItemModelRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		M mainModel = ((M) Minecraft.getInstance()
			.getItemRenderer()
			.getModel(stack, null, null, 1)); // PORT: once again scale...
		PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);

		ms.pushPose();
		ms.translate(0.5F, 0.5F, 0.5F);
		render(stack, mainModel, renderer, transformType, ms, buffer, light, overlay);
		ms.popPose();
	}

	protected abstract void render(ItemStack stack, M model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay);

}
