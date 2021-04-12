package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.effects.EffectsHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.math.vector.Matrix4f;

@Mixin(GameRenderer.class)
public abstract class StoreProjectionMatrixMixin {

	@Shadow
	private float cameraZoom;
	@Shadow
	private float zoomX;
	@Shadow
	private float zoomY;

	@Shadow
	public abstract double getFOVModifier(ActiveRenderInfo p_215311_1_, float p_215311_2_, boolean p_215311_3_);

	@Shadow
	@Final
	private Minecraft mc;
	@Shadow
	private float farPlaneDistance;

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

	@Inject(method = "getBasicProjectionMatrix",
			at = @At("HEAD"),
			cancellable = true)
	private void overrideNearPlane(ActiveRenderInfo p_228382_1_, float p_228382_2_, boolean p_228382_3_, CallbackInfoReturnable<Matrix4f> cir) {
		MatrixStack matrixstack = new MatrixStack();
		matrixstack.peek().getModel().loadIdentity();
		if (this.cameraZoom != 1.0F) {
			matrixstack.translate((double) this.zoomX, (double) (-this.zoomY), 0.0D);
			matrixstack.scale(this.cameraZoom, this.cameraZoom, 1.0F);
		}

		matrixstack.peek().getModel().multiply(Matrix4f.perspective(this.getFOVModifier(p_228382_1_, p_228382_2_, p_228382_3_), (float) this.mc.getWindow().getFramebufferWidth() / (float) this.mc.getWindow().getFramebufferHeight(), EffectsHandler.getNearPlane(), EffectsHandler.getFarPlane()));
		cir.setReturnValue(matrixstack.peek().getModel());
	}
}
