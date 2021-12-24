package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.event.MouseButtonCallback;
import com.simibubi.create.lib.event.MouseScrolledCallback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MouseHandler;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	// First return opcode is jumped over if condition is met.
	@Inject(
			method = "onPress",
			slice = @Slice(
					from = @At(
							value = "RETURN",
							ordinal = 0,
							shift = Shift.AFTER
					)
			),
			at = @At(value = "RETURN"
			)
	)
	private void create$onHandleMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		MouseButtonCallback.EVENT.invoker().onMouseButton(button, action, mods);
	}

	@Inject(
			method = "onScroll",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void create$onHandleMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci, double delta) {
		boolean cancelled = MouseScrolledCallback.EVENT.invoker().onMouseScrolled(delta);
		if (cancelled) {
			ci.cancel();
		}
	}
}
