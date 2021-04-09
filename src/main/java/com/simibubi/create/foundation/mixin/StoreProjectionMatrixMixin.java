package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.backend.Backend;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.math.vector.Matrix4f;

@Mixin(GameRenderer.class)
public class StoreProjectionMatrixMixin {

	@Unique
	private boolean shouldCopy = false;

	/**
	 * We only want to copy the projection matrix if it is going to be used to render the world.
	 * We don't care about the mat for your hand.
	 */
	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;loadProjectionMatrix(Lnet/minecraft/util/math/vector/Matrix4f;)V"))
	private void projectionMatrixReady(float p_228378_1_, long p_228378_2_, MatrixStack p_228378_4_, CallbackInfo ci) {
		shouldCopy = true;
	}

	@Inject(method = "loadProjectionMatrix", at = @At("TAIL"))
	private void onProjectionMatrixLoad(Matrix4f projection, CallbackInfo ci) {
		if (shouldCopy) {
			Backend.projectionMatrix = projection.copy();
			shouldCopy = false;
		}
	}
}
