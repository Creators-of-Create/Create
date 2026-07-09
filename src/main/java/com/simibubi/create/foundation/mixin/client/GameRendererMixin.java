package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.block.BigOutlines;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class GameRendererMixin {
	@Inject(method = "pick(F)V", at = @At("TAIL"))
	private void create$bigShapePick(float partialTicks, CallbackInfo ci) {
		BigOutlines.pick();
		TrackBlockOutline.pickCurves();
	}
}
