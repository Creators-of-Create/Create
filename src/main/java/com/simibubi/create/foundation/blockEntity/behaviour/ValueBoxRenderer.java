package com.simibubi.create.foundation.blockEntity.behaviour;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;

public class ValueBoxRenderer {

	public static void renderItemIntoValueBox(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		BakedModel modelWithOverrides = itemRenderer.getModel(filter, null, null, 0);
		boolean blockItem = modelWithOverrides.isGui3d() && modelWithOverrides.getRenderPasses(filter, false).size() <= 1;
		float scale = (!blockItem ? .5f : 1f) + 1 / 64f;
		float zOffset = (!blockItem ? -.15f : 0) + customZOffset(filter.getItem());
		ms.scale(scale, scale, scale);
		ms.translate(0, 0, zOffset);
		itemRenderer.renderStatic(filter, TransformType.FIXED, light, overlay, ms, buffer, 0);
	}

	public static void renderFlatItemIntoValueBox(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		if (filter.isEmpty())
			return;

		int bl = light >> 4 & 0xf;
		int sl = light >> 20 & 0xf;
		int itemLight = Mth.floor(sl + .5) << 20 | (Mth.floor(bl + .5) & 0xf) << 4;

		ms.pushPose();
		TransformStack.cast(ms)
			.rotateX(230);
		Matrix3f copy = ms.last()
			.normal()
			.copy();
		ms.popPose();

		ms.pushPose();
		TransformStack.cast(ms)
			.translate(0, 0, -1 / 4f)
			.translate(0, 0, 1 / 32f + .001)
			.rotateY(180);

		PoseStack squashedMS = new PoseStack();
		squashedMS.last()
			.pose()
			.multiply(ms.last()
				.pose());
		squashedMS.scale(.5f, .5f, 1 / 1024f);
		squashedMS.last()
			.normal()
			.load(copy);
		Minecraft.getInstance()
			.getItemRenderer()
			.renderStatic(filter, TransformType.GUI, itemLight, OverlayTexture.NO_OVERLAY, squashedMS, buffer, 0);

		ms.popPose();
	}

	@SuppressWarnings("deprecation")
	private static float customZOffset(Item item) {
		float nudge = -.1f;
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block instanceof AbstractSimpleShaftBlock)
				return nudge;
			if (block instanceof FenceBlock)
				return nudge;
			if (block.builtInRegistryHolder()
				.is(BlockTags.BUTTONS))
				return nudge;
			if (block == Blocks.END_ROD)
				return nudge;
		}
		return 0;
	}

}
