package com.simibubi.create.foundation.mixin.compat;

import com.simibubi.create.compat.trainmap.XaeroTrainMap;

import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;

@Mixin(GuiMap.class)
public abstract class XaeroFullscreenMapMixin {

	@Shadow(remap = false)
	private double cameraX;

	@Shadow(remap = false)
	private double cameraZ;

	@Shadow(remap = false)
	private double scale;

	@Shadow(remap = false)
	private MapProcessor mapProcessor;

	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
	public void create$xaeroMapFullscreenRender(GuiGraphics graphics, int mouseX, int mouseY, float pt, CallbackInfo ci) {
		XaeroTrainMap.onRender(graphics, (GuiMap) (Object) this, mapProcessor, cameraX, cameraZ, mouseX, mouseY, scale, pt);
	}
}
