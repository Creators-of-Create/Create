package com.simibubi.create.foundation.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.CustomRenderedArmorItem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
	// MCDev will say the @Local is invalid, it's not. MCDev doesn't know how to properly read INVOKE_ASSIGN Targets.
	@Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
	private <T extends LivingEntity, V extends HumanoidModel<T>> void create$onRenderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot slot, int light, V model, CallbackInfo ci, @Local(ordinal = 0) ItemStack stack) {
		if (stack.getItem() instanceof CustomRenderedArmorItem renderer) {
			renderer.renderArmorPiece((HumanoidArmorLayer<?, ?, ?>) (Object) this, poseStack, bufferSource, entity, slot, light, model, stack);
			ci.cancel();
		}
	}
}
