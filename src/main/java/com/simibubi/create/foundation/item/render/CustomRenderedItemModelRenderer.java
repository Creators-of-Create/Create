package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRenderedItemModelRenderer {

	public CustomRenderedItemModelRenderer() {
	}

	public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		// TODO 26.2: Rebuild custom item rendering on top of ItemModel/SpecialModelRenderer.
	}

	protected abstract void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay);

}
