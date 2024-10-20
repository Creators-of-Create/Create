package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;

@Mixin(LocalPlayer.class)
public abstract class HeavyBootsOnPlayerMixin extends AbstractClientPlayer {
	private HeavyBootsOnPlayerMixin(ClientLevel level, GameProfile profile) {
		super(level, profile);
	}

	@Inject(method = "isUnderWater()Z", at = @At("HEAD"), cancellable = true)
	private void create$noSwimmingWithHeavyBootsOn(CallbackInfoReturnable<Boolean> cir) {
		CompoundTag persistentData = getPersistentData();
		if (persistentData.contains("HeavyBoots"))
			cir.setReturnValue(false);
	}
}
