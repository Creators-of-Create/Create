package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.render.contraption.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.IInstanceRendered;
import com.simibubi.create.foundation.render.instancing.IInstancedTileEntityRenderer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class FastRenderDispatcher {

    private static Matrix4f projectionMatrixThisFrame = null;

    public static void endFrame() {
        projectionMatrixThisFrame = null;
    }

    public static void renderLayer(RenderType type, MatrixStack stack, double cameraX, double cameraY, double cameraZ) {
        Matrix4f view = Matrix4f.translate((float) -cameraX, (float) -cameraY, (float) -cameraZ);
        view.multiplyBackward(stack.peek().getModel());

        Matrix4f projection = getProjectionMatrix();

        if (type == FastKineticRenderer.getKineticRenderLayer()) {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            GL11.glCullFace(GL11.GL_BACK);
            CreateClient.kineticRenderer.render(type, projection, view);
            RenderSystem.disableCull();
            //RenderSystem.disableDepthTest();
        }

        ContraptionRenderDispatcher.renderLayer(type, projection, view);
    }

    public static void notifyLightUpdate(ClientChunkProvider world, LightType type, SectionPos pos) {
        ContraptionRenderDispatcher.notifyLightUpdate((ILightReader) world.getWorld(), type, pos);

        Chunk chunk = world.getChunk(pos.getSectionX(), pos.getSectionZ(), false);

        int sectionY = pos.getSectionY();

        if (chunk != null) {
            chunk.getTileEntityMap()
                 .entrySet()
                 .stream()
                 .filter(entry -> SectionPos.toChunk(entry.getKey().getY()) == sectionY)
                 .map(Map.Entry::getValue)
                 .forEach(FastRenderDispatcher::markForRebuild);
        }
    }

    public static void markForRebuild(TileEntity te) {
        if (te instanceof IInstanceRendered) {
            TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);

            if (renderer instanceof IInstancedTileEntityRenderer) {
                markForRebuild(te, (IInstancedTileEntityRenderer<? super TileEntity>) renderer);
            }
        }
    }

    private static <T extends TileEntity> void markForRebuild(T te, IInstancedTileEntityRenderer<T> renderer) {
        CreateClient.kineticRenderer.dirty = true;
        renderer.markForRebuild(new InstanceContext.World<>(te));
    }

    // copied from GameRenderer.renderWorld
    private static Matrix4f getProjectionMatrix() {
        if (projectionMatrixThisFrame != null) return projectionMatrixThisFrame;

        float partialTicks = AnimationTickHolder.getPartialTicks();
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        ClientPlayerEntity player = mc.player;

        MatrixStack matrixstack = new MatrixStack();
        matrixstack.peek().getModel().multiply(gameRenderer.func_228382_a_(gameRenderer.getActiveRenderInfo(), partialTicks, true));
        gameRenderer.bobViewWhenHurt(matrixstack, partialTicks);
        if (mc.gameSettings.viewBobbing) {
            gameRenderer.bobView(matrixstack, partialTicks);
        }

        float portalTime = MathHelper.lerp(partialTicks, player.prevTimeInPortal, player.timeInPortal);
        if (portalTime > 0.0F) {
            int i = 20;
            if (player.isPotionActive(Effects.NAUSEA)) {
                i = 7;
            }

            float f1 = 5.0F / (portalTime * portalTime + 5.0F) - portalTime * 0.04F;
            f1 = f1 * f1;
            Vector3f vector3f = new Vector3f(0.0F, MathHelper.SQRT_2 / 2.0F, MathHelper.SQRT_2 / 2.0F);
            matrixstack.multiply(vector3f.getDegreesQuaternion(((float)gameRenderer.rendererUpdateCount + partialTicks) * (float)i));
            matrixstack.scale(1.0F / f1, 1.0F, 1.0F);
            float f2 = -((float)gameRenderer.rendererUpdateCount + partialTicks) * (float)i;
            matrixstack.multiply(vector3f.getDegreesQuaternion(f2));
        }

        Matrix4f matrix4f = matrixstack.peek().getModel();
        gameRenderer.func_228379_a_(matrix4f);

        projectionMatrixThisFrame = matrix4f;
        return projectionMatrixThisFrame;
    }
}
