package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.render.contraption.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.gl.backend.Backend;
import com.simibubi.create.foundation.render.gl.backend.OptifineHandler;
import com.simibubi.create.foundation.render.light.ILightListener;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.WorldAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class FastRenderDispatcher {

    public static WorldAttached<ConcurrentHashMap.KeySetView<TileEntity, Boolean>> queuedUpdates = new WorldAttached<>(ConcurrentHashMap::newKeySet);
    public static WorldAttached<ConcurrentHashMap.KeySetView<TileEntity, Boolean>> addedLastTick = new WorldAttached<>(ConcurrentHashMap::newKeySet);

    private static Matrix4f projectionMatrixThisFrame = null;

    public static void endFrame() {
        projectionMatrixThisFrame = null;
    }

    public static void enqueueUpdate(TileEntity te) {
        queuedUpdates.get(te.getWorld()).add(te);
    }

    public static void tick() {
        ClientWorld world = Minecraft.getInstance().world;

        runQueue(addedLastTick.get(world), CreateClient.kineticRenderer::onLightUpdate);
        CreateClient.kineticRenderer.clean();

        runQueue(queuedUpdates.get(world), CreateClient.kineticRenderer::update);
    }

    public static boolean available() {
        return Backend.enabled;
    }

    public static void refresh() {
        RenderWork.enqueue(() -> {
            CreateClient.kineticRenderer.invalidate();
            OptifineHandler.refresh();
            Minecraft.getInstance().worldRenderer.loadRenderers();
            ClientWorld world = Minecraft.getInstance().world;
            if (world != null) world.loadedTileEntityList.forEach(CreateClient.kineticRenderer::add);
        });
    }

    private static <T> void runQueue(@Nullable ConcurrentHashMap.KeySetView<T, Boolean> changed, Consumer<T> action) {
        if (changed == null) return;

        if (available()) {
            // because of potential concurrency issues, we make a copy of what's in the set at the time we get here
            ArrayList<T> tiles = new ArrayList<>(changed);

            tiles.forEach(action);
            changed.removeAll(tiles);
        } else {
            changed.clear();
        }
    }

    public static void renderLayer(RenderType type, MatrixStack stack, double cameraX, double cameraY, double cameraZ) {
        if (!available()) return;

        Matrix4f viewProjection = Matrix4f.translate((float) -cameraX, (float) -cameraY, (float) -cameraZ);
        viewProjection.multiplyBackward(stack.peek().getModel());
        viewProjection.multiplyBackward(getProjectionMatrix());

        type.startDrawing();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        GL11.glCullFace(GL11.GL_BACK);
        CreateClient.kineticRenderer.render(type, viewProjection);
        RenderSystem.disableCull();
        //RenderSystem.disableDepthTest();

        ContraptionRenderDispatcher.renderLayer(type, viewProjection);
        GL20.glUseProgram(0);
        type.endDrawing();
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
                 .filter(tile -> tile instanceof ILightListener)
                 .map(tile -> (ILightListener) tile)
                 .forEach(ILightListener::onChunkLightUpdate);
        }
    }

    // copied from GameRenderer.renderWorld
    public static Matrix4f getProjectionMatrix() {
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
