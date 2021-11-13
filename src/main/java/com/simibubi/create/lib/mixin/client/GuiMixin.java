package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.lib.event.OverlayRenderCallback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Unique
	public float create$partialTicks;

	@Inject(at = @At("HEAD"),
			method = "render")
	public void create$render(PoseStack matrixStack, float f, CallbackInfo ci) {
		create$partialTicks = f;
	}

	//This might be the wrong method to inject to
	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"),
			method = "renderPlayerHealth")
	private void create$renderStatusBars(PoseStack matrixStack, CallbackInfo ci) {
		OverlayRenderCallback.EVENT.invoker().onOverlayRender(matrixStack, create$partialTicks, minecraft.getWindow(), OverlayRenderCallback.Types.AIR);
	}

	@Inject(at = @At("HEAD"),
			method = "renderCrosshair")
	private void create$renderCrosshair(PoseStack matrixStack, CallbackInfo ci) {
		OverlayRenderCallback.EVENT.invoker().onOverlayRender(matrixStack, create$partialTicks, minecraft.getWindow(), OverlayRenderCallback.Types.CROSSHAIRS);
	}
}
