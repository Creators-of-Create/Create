package com.simibubi.create.foundation.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.math.vector.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.render.backend.Backend;

@Mixin(GameRenderer.class)
public class StoreProjectionMatrixMixin {

	@Inject(method = "loadProjectionMatrix", at = @At("TAIL"))
	private void onProjectionMatrixLoad(Matrix4f projection, CallbackInfo ci) {
		Backend.projectionMatrix = projection.copy();
	}
}
