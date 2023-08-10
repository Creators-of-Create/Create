package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(PoseStack.class)
public class FixNormalScalingMixin {
	/**
	 * Minecraft negates the normal matrix if all scales are equal and negative, but
	 * does not return afterward. This allows the rest of the method's logic to be
	 * applied, which negates the matrix again, resulting in the matrix being the
	 * same as in the beginning.
	 */
	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/math/Matrix3f;mul(F)V", shift = Shift.AFTER), method = "scale(FFF)V", cancellable = true)
	private void create$returnAfterNegate(float x, float y, float z, CallbackInfo ci) {
		ci.cancel();
	}

	/**
	 * Minecraft takes the inverse cube root of the product of all scales to provide a
	 * rough estimate for normalization so that it does not need to be done later. It
	 * does not make sense for this "normalization factor" to be negative though, as
	 * that would invert all normals. Additionally, Minecraft's fastInverseCbrt method
	 * does not work for negative numbers.
	 */
	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;fastInvCubeRoot(F)F"), method = "scale(FFF)V")
	private float create$absInvCbrtInput(float input) {
		return Math.abs(input);
	}
}
