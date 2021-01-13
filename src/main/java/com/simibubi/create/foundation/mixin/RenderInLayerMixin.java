package com.simibubi.create.foundation.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class RenderInLayerMixin {
    @Shadow
    @Inject(at = @At("HEAD"), method = "renderLayer")  // (Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/matrix/MatrixStack;DDD)V")
    private void renderLayer(RenderType type, MatrixStack stack, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {

    }
}
