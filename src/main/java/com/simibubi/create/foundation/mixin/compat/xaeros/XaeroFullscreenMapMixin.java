package com.simibubi.create.foundation.mixin.compat.xaeros;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.Create;
import com.simibubi.create.compat.trainmap.XaeroTrainMap;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import xaero.map.gui.GuiMap;

@Mixin(GuiMap.class)
public abstract class XaeroFullscreenMapMixin {
	@Unique
	private boolean create$failedToRenderTrainMap = false;

	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lnet/minecraft/resources/Identifier;IIIIII)V"), require = 0)
	private void create$xaeroMapFullscreenRender(GuiGraphicsExtractor GuiGraphicsExtractor, int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
		try {
			if (!create$failedToRenderTrainMap)
				XaeroTrainMap.onRender(GuiGraphicsExtractor, (GuiMap) (Object) this, scaledMouseX, scaledMouseY, partialTicks);
		} catch (Throwable e) {
			Create.LOGGER.error("Failed to render Xaero's World Map train map integration:", e);
			create$failedToRenderTrainMap = true;
		}
	}
}
