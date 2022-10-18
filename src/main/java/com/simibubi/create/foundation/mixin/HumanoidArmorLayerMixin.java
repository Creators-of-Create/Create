package com.simibubi.create.foundation.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.MultiLayeredArmorItem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
	@Shadow
	@Final
	private HumanoidModel<?> innerModel;
	@Shadow
	@Final
	private HumanoidModel<?> outerModel;

	@Unique
	private boolean intercepted;
	@Unique
	private Boolean useInnerTexture;

	@Shadow
	private void renderArmorPiece(PoseStack poseStack, MultiBufferSource buffer, LivingEntity livingEntity, EquipmentSlot slot, int packedLight, HumanoidModel<?> model) {
	}

	@Shadow
	private boolean usesInnerModel(EquipmentSlot slot) {
		return false;
	}

	@Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;getParentModel()Lnet/minecraft/client/model/EntityModel;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void onRenderArmorPiece(PoseStack poseStack, MultiBufferSource buffer, LivingEntity livingEntity, EquipmentSlot slot, int packedLight, HumanoidModel<?> model, CallbackInfo ci, ItemStack stack, ArmorItem armorItem) {
		if (intercepted) {
			return;
		}

		if (armorItem instanceof MultiLayeredArmorItem) {
			intercepted = true;

			useInnerTexture = true;
			renderArmorPiece(poseStack, buffer, livingEntity, slot, packedLight, innerModel);
			useInnerTexture = false;
			renderArmorPiece(poseStack, buffer, livingEntity, slot, packedLight, outerModel);

			useInnerTexture = null;
			intercepted = false;
			ci.cancel();
		}
	}

	@Inject(method = "usesInnerModel", at = @At("HEAD"), cancellable = true)
	private void onUsesInnerModel(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
		if (useInnerTexture != null) {
			cir.setReturnValue(useInnerTexture);
		}
	}

	@ModifyVariable(method = "getArmorResource", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;getArmorTexture(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/lang/String;)Ljava/lang/String;", shift = Shift.BEFORE), ordinal = 0, remap = false)
	private String modifyType(@Nullable String type, Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String typeArg) {
		if (stack.getItem() instanceof MultiLayeredArmorItem) {
			return usesInnerModel(slot) ? "2" : "1";
		}

		return type;
	}
}
