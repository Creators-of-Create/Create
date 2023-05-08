package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class HeavyBootsOnPlayerMixin extends AbstractClientPlayer {

	private HeavyBootsOnPlayerMixin(ClientLevel level, GameProfile profile, ProfilePublicKey profileKey) {
		super(level, profile, profileKey);
	}

	@Inject(method = "isUnderWater()Z", at = @At("HEAD"), cancellable = true)
	public void noSwimmingWithHeavyBootsOn(CallbackInfoReturnable<Boolean> cir) {
		CompoundTag persistentData = getPersistentData();
		if (persistentData.contains("HeavyBoots"))
			cir.setReturnValue(false);
	}

}
