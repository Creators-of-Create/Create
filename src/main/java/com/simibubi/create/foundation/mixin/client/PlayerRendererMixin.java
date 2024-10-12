package com.simibubi.create.foundation.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.model.HumanoidModel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.foundation.item.CustomArmPoseItem;

import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
	@Inject(
			method = "getArmPose(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
			),
			cancellable = true
	)
	private static void create$onGetArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<ArmPose> cir, @Local ItemStack stack) {
		if (stack.getItem() instanceof CustomArmPoseItem armPoseProvider) {
			ArmPose pose = armPoseProvider.getArmPose(stack, player, hand);
			if (pose != null) {
				cir.setReturnValue(pose);
			}
		}
	}
}
