package com.simibubi.create.foundation.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(WorldRenderer.class)
public class RenderInLayerMixin {

    /**
     * JUSTIFICATION: This method is called once per layer per frame. It allows us to perform
     * layer-correct custom rendering. RenderWorldLast is not refined enough for rendering world objects.
     * This should probably be a forge event.
     */
    @Inject(at = @At(value = "TAIL"), method = "renderLayer")
    private void renderLayer(RenderType type, MatrixStack stack, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        FastRenderDispatcher.renderLayer(type, stack, (float) cameraX, (float) cameraY, (float) cameraZ);
    }
}
