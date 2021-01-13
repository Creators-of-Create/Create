package com.simibubi.create.foundation.mixin;

import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkProvider.class)
public class LightUpdateMixin {
    @Shadow
    @Inject(at = @At("HEAD"), method = "markLightChanged(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/SectionPos;)V")
    private void onLightUpdate(LightType type, SectionPos pos, CallbackInfo ci) {

    }
}
