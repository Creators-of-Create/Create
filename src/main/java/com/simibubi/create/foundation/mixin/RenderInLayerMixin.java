package com.simibubi.create.foundation.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class RenderInLayerMixin {

    @Inject(at = @At("HEAD"), method = "renderLayer")
    private void renderLayer(RenderType type, MatrixStack stack, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {

    }
}
