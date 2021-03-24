package com.simibubi.create.foundation.mixin;

import com.simibubi.create.foundation.render.KineticRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.OptifineHandler;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(WorldRenderer.class)
public class RenderHooksMixin {

    @Shadow private ClientWorld world;

    /**
     * JUSTIFICATION: This method is called once per layer per frame. It allows us to perform
     * layer-correct custom rendering. RenderWorldLast is not refined enough for rendering world objects.
     * This should probably be a forge event.
     */
    @Inject(at = @At("TAIL"), method = "renderLayer")
    private void renderLayer(RenderType type, MatrixStack stack, double camX, double camY, double camZ, CallbackInfo ci) {
        if (!Backend.available()) return;

        Matrix4f viewProjection = stack.peek().getModel().copy();
        viewProjection.multiplyBackward(FastRenderDispatcher.getProjectionMatrix());

        FastRenderDispatcher.renderLayer(type, viewProjection, camX, camY, camZ);

        ContraptionRenderDispatcher.renderLayer(type, viewProjection, camX, camY, camZ);

        GL20.glUseProgram(0);
    }

    @Inject(at = @At(value = "INVOKE", target = "net.minecraft.client.renderer.WorldRenderer.updateChunks(J)V"), method = "render")
    private void setupFrame(MatrixStack p_228426_1_, float p_228426_2_, long p_228426_3_, boolean p_228426_5_, ActiveRenderInfo info, GameRenderer p_228426_7_, LightTexture p_228426_8_, Matrix4f p_228426_9_, CallbackInfo ci) {
        Vector3d cameraPos = info.getProjectedView();
        double camX = cameraPos.getX();
        double camY = cameraPos.getY();
        double camZ = cameraPos.getZ();

        CreateClient.kineticRenderer.get(world).beginFrame(camX, camY, camZ);
        ContraptionRenderDispatcher.beginFrame(camX, camY, camZ);
    }

    @Inject(at = @At("TAIL"), method = "scheduleBlockRerenderIfNeeded")
    private void checkUpdate(BlockPos pos, BlockState lastState, BlockState newState, CallbackInfo ci) {
        CreateClient.kineticRenderer.get(world).update(world.getTileEntity(pos));
    }

    @Inject(at = @At("TAIL"), method = "loadRenderers")
    private void refresh(CallbackInfo ci) {
        ContraptionRenderDispatcher.invalidateAll();
        OptifineHandler.refresh();
        Backend.refresh();

        if (Backend.canUseInstancing() && world != null) {
            KineticRenderer kineticRenderer = CreateClient.kineticRenderer.get(world);
            kineticRenderer.invalidate();
            world.loadedTileEntityList.forEach(kineticRenderer::add);
        }
    }
}
