package com.simibubi.create.foundation.mixin.client;

import net.createmod.catnip.config.ui.ConfigScreenList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ConfigScreenList.class, remap = false)
public class ConfigScreenListMixin {

	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void create$unfocusConfigText(double x, double y, int button,
										  CallbackInfoReturnable<Boolean> cir) {

		if (ConfigScreenList.currentText != null
			&& !ConfigScreenList.currentText.isMouseOver(x, y)) {
			ConfigScreenList.currentText.setFocused(false);
		}
	}
}
