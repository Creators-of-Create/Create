package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.block.BigOutlines;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(at = @At("TAIL"), method = "pick")
	private void bigShapePick(CallbackInfo ci) {
		BigOutlines.pick();
		TrackBlockOutline.pickCurves();
	}
}
