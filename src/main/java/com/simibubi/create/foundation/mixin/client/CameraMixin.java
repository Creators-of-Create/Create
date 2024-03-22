package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.simibubi.create.content.trains.CameraDistanceModifier;

import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@ModifyArg(
			method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"),
			index = 0
	)
	private double create$modifyCameraOffset(double originalValue) {
		return originalValue * CameraDistanceModifier.getMultiplier();
	}
}
