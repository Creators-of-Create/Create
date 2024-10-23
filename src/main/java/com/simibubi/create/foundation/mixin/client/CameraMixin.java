package com.simibubi.create.foundation.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.simibubi.create.content.trains.CameraDistanceModifier;

import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@ModifyExpressionValue(
			method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"
			)
	)
	private double create$modifyCameraOffset(double originalValue) {
		return originalValue * CameraDistanceModifier.getMultiplier();
	}
}
