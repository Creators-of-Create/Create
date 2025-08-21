package com.simibubi.create.foundation.blockEntity.behaviour;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;

public class ValueBoxRenderer {

	public static void renderItemIntoValueBox(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		BakedModel modelWithOverrides = itemRenderer.getModel(filter, null, null, 0);
		boolean blockItem = modelWithOverrides.isGui3d();
		float scale = (!blockItem ? .5f : 1f) + 1 / 64f;
		float zOffset = !blockItem ? -.15f : customZOffset(filter.getItem());
		ms.scale(scale, scale, scale);
		ms.translate(0, 0, zOffset);
		itemRenderer.render(filter, ItemDisplayContext.FIXED, false, ms, buffer, light, overlay, modelWithOverrides);
	}

	public static void renderFlatItemIntoValueBox(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		if (filter.isEmpty()) return;
		TransformStack.of(ms)
			.translate(0, 0, -1 / 4f + 1 / 32f + .001)
			.rotateYDegrees(180)
			.scale(.5f, .5f, 1 / 1024f);
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		itemRenderer.renderStatic(filter, ItemDisplayContext.GUI, light, OverlayTexture.NO_OVERLAY, ms, buffer, mc.level, 0);
	}

	private static float customZOffset(Item item) {
		final float nudge = -.1f;
		if (!(item instanceof BlockItem blockItem)) return 0f;
		// Special case : gears are thick enough but need to be offset anyway
		Block block = blockItem.getBlock();
		if (block instanceof AbstractSimpleShaftBlock) return nudge;
		// General case : determine offset based on shape thickness
		VoxelShape shape = block.defaultBlockState().getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
		if (shape.isEmpty()) return 0f;
		double thickness = shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z);
		return thickness <= .25 ? nudge : 0f;
	}
}
