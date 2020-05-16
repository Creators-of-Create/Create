package com.simibubi.create.foundation.behaviour;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.modules.logistics.item.filter.FilterItem;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;

@SuppressWarnings("deprecation")
public class ValueBoxRenderer {

	public static void renderItemIntoValueBox(ItemStack filter, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		IBakedModel modelWithOverrides = itemRenderer.getItemModelWithOverrides(filter, Minecraft.getInstance().world, null);
		boolean blockItem = modelWithOverrides.isGui3d();
		float scale = (!blockItem ? .5f : 1f) - 1 / 64f;
		float zOffset = (!blockItem ? -.225f : 0) + customZOffset(filter.getItem());
		ms.scale(scale, scale, scale);
		ms.translate(0, 0, zOffset);
		itemRenderer.renderItem(filter, TransformType.FIXED, light, overlay, ms, buffer);
	}

	private static float customZOffset(Item item) {
		float NUDGE = -.1f;
		if (item instanceof FilterItem)
			return NUDGE;
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block instanceof ShaftBlock)
				return NUDGE;
			if (block instanceof FenceBlock)
				return NUDGE;
			if (block.isIn(BlockTags.BUTTONS))
				return NUDGE;
			if (block == Blocks.END_ROD)
				return NUDGE;
		}
		return 0;
	}

}
