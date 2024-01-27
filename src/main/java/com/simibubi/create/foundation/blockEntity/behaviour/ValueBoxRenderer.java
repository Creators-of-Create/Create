package com.simibubi.create.foundation.blockEntity.behaviour;

import org.joml.Matrix3f;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;

public class ValueBoxRenderer {

	public static void renderItemIntoValueBox(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		BakedModel modelWithOverrides = itemRenderer.getModel(filter, null, null, 0);
		boolean blockItem =
			modelWithOverrides.isGui3d() && modelWithOverrides.getRenderPasses(filter, false).size() <= 1;
		float scale = (!blockItem ? .5f : 1f) + 1 / 64f;
		float zOffset = (!blockItem ? -.15f : 0) + customZOffset(filter.getItem());
		ms.scale(scale, scale, scale);
		ms.translate(0, 0, zOffset);
		itemRenderer.renderStatic(filter, ItemDisplayContext.FIXED, light, overlay, ms, buffer, mc.level, 0);
	}

	public static void renderFlatItemIntoValueBox(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		if (filter.isEmpty())
			return;

		int bl = light >> 4 & 0xf;
		int sl = light >> 20 & 0xf;
		int itemLight = Mth.floor(sl + .5) << 20 | (Mth.floor(bl + .5) & 0xf) << 4;

		ms.pushPose();
		TransformStack.of(ms)
			.rotateXDegrees(230);
		Matrix3f copy = new Matrix3f(ms.last()
			.normal());
		ms.popPose();

		ms.pushPose();
		TransformStack.of(ms)
			.translate(0, 0, -1 / 4f)
			.translate(0, 0, 1 / 32f + .001)
			.rotateYDegrees(180);

		PoseStack squashedMS = new PoseStack();
		squashedMS.last()
			.pose()
			.mul(ms.last()
				.pose());
		squashedMS.scale(.5f, .5f, 1 / 1024f);
		squashedMS.last()
			.normal()
			.set(copy);
		Minecraft mc = Minecraft.getInstance();
		mc.getItemRenderer()
			.renderStatic(filter, ItemDisplayContext.GUI, itemLight, OverlayTexture.NO_OVERLAY, squashedMS, buffer, mc.level, 0);

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
