package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.model.BakedModel;

import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Temporary Create 26.2 porting bridge for legacy item-render callers.
 * TODO 26.2: Remove when migrated to ItemModelResolver and ItemStackRenderState.
 */
@Deprecated(forRemoval = true)
public class ItemRenderer {
	private static final BakedModel EMPTY_MODEL = new BakedModel() {};

	public static VertexConsumer getFoilBuffer(MultiBufferSource buffer, RenderType renderType, boolean isItem,
		boolean glint) {
		return buffer.getBuffer(renderType);
	}

	public BakedModel getModel(ItemStack stack, Level level, LivingEntity entity, int seed) {
		return EMPTY_MODEL;
	}

	public void render(ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack,
		MultiBufferSource buffer, int light, int overlay, BakedModel model) {
	}

	public void renderStatic(ItemStack stack, ItemDisplayContext displayContext, int light, int overlay,
		PoseStack poseStack, MultiBufferSource buffer, Level level, int seed) {
	}
}
