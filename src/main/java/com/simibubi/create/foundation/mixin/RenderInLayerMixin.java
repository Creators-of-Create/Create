package com.simibubi.create.foundation.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.OptifineHandler;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;
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
    private void renderLayer(RenderType type, MatrixStack stack, double camX, double camY, double camZ, CallbackInfo ci) {
        if (!Backend.available()) return;

        float cameraX = (float) camX;
        float cameraY = (float) camY;
        float cameraZ = (float) camZ;

        Matrix4f viewProjection = Matrix4f.translate(-cameraX, -cameraY, -cameraZ);
        viewProjection.multiplyBackward(stack.peek().getModel());
        viewProjection.multiplyBackward(FastRenderDispatcher.getProjectionMatrix());

        FastRenderDispatcher.renderLayer(type, viewProjection, cameraX, cameraY, cameraZ);

        ContraptionRenderDispatcher.renderLayer(type, viewProjection, cameraX, cameraY, cameraZ);

        GL20.glUseProgram(0);
    }
}
