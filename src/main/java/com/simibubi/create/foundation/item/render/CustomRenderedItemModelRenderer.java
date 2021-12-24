package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRenderedItemModelRenderer<M extends CustomRenderedItemModel> implements DynamicItemRenderer {

	@Override
	@SuppressWarnings("unchecked")
	public void render(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		M mainModel = (M) Minecraft.getInstance()
			.getItemRenderer()
			.getModel(stack, null, null, 0);
		PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);

		ms.pushPose();
		ms.translate(0.5F, 0.5F, 0.5F);
		render(stack, mainModel, renderer, transformType, ms, buffer, light, overlay);
		ms.popPose();
	}

	protected abstract void render(ItemStack stack, M model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay);

	public abstract M createModel(BakedModel originalModel);



}
