package com.simibubi.create.foundation.tileEntity.behaviour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractSimpleShaftBlock;
import com.simibubi.create.content.logistics.item.filter.FilterItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraftforge.client.model.ItemMultiLayerBakedModel;

public class ValueBoxRenderer {

	public static void renderItemIntoValueBox(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		BakedModel modelWithOverrides = itemRenderer.getModel(filter, null, null, 0);
		boolean blockItem = modelWithOverrides.isGui3d() && !(modelWithOverrides instanceof ItemMultiLayerBakedModel);
		float scale = (!blockItem ? .5f : 1f) - 1 / 64f;
		float zOffset = (!blockItem ? -.225f : 0) + customZOffset(filter.getItem());
		ms.scale(scale, scale, scale);
		ms.translate(0, 0, zOffset);
		itemRenderer.renderStatic(filter, TransformType.FIXED, light, overlay, ms, buffer, 0);
	}

	@SuppressWarnings("deprecation")
	private static float customZOffset(Item item) {
		float nudge = -.1f;
		if (item instanceof FilterItem)
			return nudge;
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block instanceof AbstractSimpleShaftBlock)
				return nudge;
			if (block instanceof FenceBlock)
				return nudge;
			if (block.builtInRegistryHolder().is(BlockTags.BUTTONS))
				return nudge;
			if (block == Blocks.END_ROD)
				return nudge;
		}
		return 0;
	}

}
