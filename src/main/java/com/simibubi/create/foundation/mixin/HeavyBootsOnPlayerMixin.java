package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.lib.util.EntityHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class HeavyBootsOnPlayerMixin extends AbstractClientPlayer {

	private HeavyBootsOnPlayerMixin(ClientLevel level, GameProfile profile) {
		super(level, profile);
	}

	@Inject(at = @At("HEAD"), method = "isUnderWater", cancellable = true)
	public void noSwimmingWithHeavyBootsOn(CallbackInfoReturnable<Boolean> cir) {
		CompoundTag persistentData = EntityHelper.getExtraCustomData(this);
		if (persistentData.contains("HeavyBoots"))
			cir.setReturnValue(false);
	}

}
