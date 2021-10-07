package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.curiosities.weapons.PotatoProjectileTypeManager;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/ServerRecipeBook;sendInitialRecipeBook(Lnet/minecraft/entity/player/ServerPlayerEntity;)V", shift = At.Shift.AFTER), method = "placeNewPlayer(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/entity/player/ServerPlayerEntity;)V")
	private void afterSendRecipeBookOnPlaceNewPlayer(NetworkManager networkManager, ServerPlayerEntity player, CallbackInfo ci) {
		PotatoProjectileTypeManager.syncTo(player);
	}

	@Inject(at = @At("TAIL"), method = "reloadResources()V")
	private void onReloadResources(CallbackInfo ci) {
		PotatoProjectileTypeManager.syncToAll();
	}
}
