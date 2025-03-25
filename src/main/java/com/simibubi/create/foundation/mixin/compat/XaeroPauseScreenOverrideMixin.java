package com.simibubi.create.foundation.mixin.compat;

import com.simibubi.create.compat.Mods;

import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xaero.map.gui.ScreenBase;

@Mixin(Screen.class)
public class XaeroPauseScreenOverrideMixin {

	@Inject(method = "isPauseScreen", at = @At("HEAD"), cancellable = true)
	public void create$XaeroScreenPauseOverride(CallbackInfoReturnable<Boolean> cir){
		if(Mods.XAEROWORLDMAP.isLoaded()) {
			if ((Object) this instanceof ScreenBase) {
				cir.setReturnValue(false);
			}
		}
	}
}
