package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.lib.render.TransformTypeDependentItemBakedModel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemRenderer.class, priority = 10000)
public abstract class ItemRendererMixin {
	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
	private BakedModel create$handleModel(BakedModel model, ItemStack itemStack, ItemTransforms.TransformType transformType, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model1) {
		if (model instanceof TransformTypeDependentItemBakedModel handler) {
			return handler.create$handlePerspective(transformType);
		}
		return model;
	}
}
