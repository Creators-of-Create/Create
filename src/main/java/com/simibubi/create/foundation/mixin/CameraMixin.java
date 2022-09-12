package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.simibubi.create.content.logistics.trains.CameraDistanceModifier;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@ModifyArg(
			method = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"),
			index = 0
	)
	public double modifyCameraOffset(double originalValue) {
		return originalValue * CameraDistanceModifier.getMultiplier(AnimationTickHolder.getPartialTicks());
	}
}
