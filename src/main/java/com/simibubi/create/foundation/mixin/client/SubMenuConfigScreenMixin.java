package com.simibubi.create.foundation.mixin.client;

import net.createmod.catnip.config.ui.ConfigScreenList;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SubMenuConfigScreen.class, remap = false)
public abstract class SubMenuConfigScreenMixin {

	@Shadow
	protected ConfigScreenList list;

	@Inject(method = "getFocused", at = @At("HEAD"), cancellable = true)
	private void create$focusConfigList(
		CallbackInfoReturnable<GuiEventListener> cir
	) {
		if (ConfigScreenList.currentText == null && list != null) {
			cir.setReturnValue(list);
		}
	}
}
