package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.lib.event.FogEvents;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

	@Shadow
	private static float fogRed;

	@Shadow
	private static float fogGreen;

	@Shadow
	private static float fogBlue;

//TODO: Move this idk where
//	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"),
//			method = "render(Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/world/ClientWorld;IF)V")
//	private static void render(Camera activeRenderInfo, float f, ClientLevel clientWorld, int i, float g, CallbackInfo ci) {
//		Vector3f color = FogEvents.SET_COLOR.invoker().setColor(activeRenderInfo, new Vector3f(red, green, blue));
//		red = color.x();
//		green = color.y();
//		blue = color.z();
//	}

	@Inject(at = @At("HEAD"),
			method = "setupFog", cancellable = true)
	private static void setupFog(Camera activeRenderInfo, FogRenderer.FogMode fogType, float f, boolean bl, CallbackInfo ci) {
		float density = FogEvents.SET_DENSITY.invoker().setDensity(activeRenderInfo, 0.1f);
		if (density != 0.1f) {
			//I am not 100% sure this is the same as RenderSystem.fogDensity(density) ¯\_(ツ)_/¯
			RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue, density);
			//RenderSystem.fogDensity(density);
			ci.cancel();
		}
	}
}
