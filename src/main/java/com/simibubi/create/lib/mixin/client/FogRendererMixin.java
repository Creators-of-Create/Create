package com.simibubi.create.lib.mixin.client;

import com.simibubi.create.lib.event.FogEvents.ColorData;

import net.minecraft.client.multiplayer.ClientLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.lib.event.FogEvents;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;

import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
	@Shadow
	private static float fogRed;

	@Shadow
	private static float fogGreen;

	@Shadow
	private static float fogBlue;

	@ModifyArgs(method = "setupColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
	private static void create$modifyFogColors(Args args, Camera camera, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier) {
		ColorData data = new ColorData(camera, fogRed, fogGreen, fogBlue);
		FogEvents.SET_COLOR.invoker().setColor(data);
		fogRed = data.getRed();
		fogGreen = data.getGreen();
		fogBlue = data.getBlue();
	}

	@Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
	private static void create$setupFog(Camera activeRenderInfo, FogRenderer.FogMode fogType, float f, boolean bl, CallbackInfo ci) {
		float density = FogEvents.SET_DENSITY.invoker().setDensity(activeRenderInfo, 0.1f);
		if (density != 0.1f) {
			RenderSystem.setShaderFogStart(-8.0F);
			RenderSystem.setShaderFogEnd(density * 0.5F);
			ci.cancel();
		}
	}
}
