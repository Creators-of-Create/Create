package com.simibubi.create.foundation.mixin.compat.xaeros;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.trainmap.XaeroTrainMap;

import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public class XaeroPauseScreenOverrideMixin {

	@Inject(method = "isPauseScreen", at = @At("HEAD"), cancellable = true)
	public void create$xaeroPauseScreenOverride(CallbackInfoReturnable<Boolean> cir) {
		if (Mods.XAEROWORLDMAP.isLoaded()) {
			if (XaeroTrainMap.isMapOpen((Screen) (Object) this))
				cir.setReturnValue(false);
		}
	}
}
