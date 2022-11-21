package com.simibubi.create.foundation.mixin;

import com.simibubi.create.content.curiosities.zapper.ZapperItem;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.curiosities.weapons.PotatoCannonItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;

@Mixin(PlayerRenderer.class)
public class PlayerEntityRendererMixin {
	@Inject(
			method = "getArmPose",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void getArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> ci) {
		Item handItem = player.getItemInHand(hand).getItem();
		if (!player.swinging && (handItem instanceof PotatoCannonItem || handItem instanceof ZapperItem))
			ci.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
	}
}

