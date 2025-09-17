package com.simibubi.create.foundation.mixin.compat.xaeros;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.Create;
import com.simibubi.create.compat.trainmap.XaeroTrainMap;

import net.minecraft.client.gui.GuiGraphics;
import xaero.map.gui.GuiMap;

@Mixin(GuiMap.class)
public abstract class XaeroFullscreenMapMixin {
	@Unique
	boolean create$failedToRenderTrainMap = false;

	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), require = 0)
	public void create$xaeroMapFullscreenRender(GuiGraphics graphics, int mouseX, int mouseY, float pt, CallbackInfo ci) {
		try {
			if (!create$failedToRenderTrainMap)
				XaeroTrainMap.onRender(graphics, (GuiMap) (Object) this, mouseX, mouseY, pt);
		} catch (Exception e) {
			Create.LOGGER.error("Failed to render Xaero's World Map train map integration:", e);
			create$failedToRenderTrainMap = true;
		}
	}
}
