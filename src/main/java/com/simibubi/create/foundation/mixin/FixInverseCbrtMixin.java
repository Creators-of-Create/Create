package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.math.MathHelper;

/**
 * Vanilla's fast inverse cube root function returns nonsensical results for negative
 * numbers, which results in incorrect vertex normal scaling. By negating the input
 * and output accordingly, this issue can be prevented.
 */
@Mixin(MathHelper.class)
public class FixInverseCbrtMixin {
	@ModifyVariable(at = @At("HEAD"), method = "fastInverseCbrt(F)F")
	private static float negateAtHead(float input) {
		if (input < 0) {
			input *= -1;
		}
		return input;
	}

	@Inject(at = @At("TAIL"), method = "fastInverseCbrt(F)F", cancellable = true)
	private static void negateAtTail(float input, CallbackInfoReturnable<Float> cir) {
		if (input < 0) {
			cir.setReturnValue(cir.getReturnValueF() * -1);
		}
	}
}
