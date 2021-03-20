package com.simibubi.create.foundation.render.backend;

import java.util.concurrent.ConcurrentHashMap;

import com.simibubi.create.foundation.render.KineticRenderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class FastRenderDispatcher {

    public static WorldAttached<ConcurrentHashMap.KeySetView<TileEntity, Boolean>> queuedUpdates = new WorldAttached<>(ConcurrentHashMap::newKeySet);

    private static Matrix4f projectionMatrixThisFrame = null;

    public static void endFrame() {
        projectionMatrixThisFrame = null;
    }

    public static void enqueueUpdate(TileEntity te) {
        queuedUpdates.get(te.getWorld()).add(te);
    }

    public static void tick() {
        ClientWorld world = Minecraft.getInstance().world;

        KineticRenderer kineticRenderer = CreateClient.kineticRenderer.get(world);
        kineticRenderer.tick();

        ConcurrentHashMap.KeySetView<TileEntity, Boolean> map = queuedUpdates.get(world);
        map
                .forEach(te -> {
                    map.remove(te);

                    kineticRenderer.update(te);
                });
    }

    public static boolean available() {
        return Backend.canUseInstancing();
    }

    public static boolean available(World world) {
        return Backend.canUseInstancing() && Backend.isFlywheelWorld(world);
    }

    public static int getDebugMode() {
        return KineticDebugger.isActive() ? 1 : 0;
    }

    public static void refresh() {
        RenderWork.enqueue(Minecraft.getInstance().worldRenderer::loadRenderers);
    }

    public static void renderLayer(RenderType layer, Matrix4f viewProjection, double cameraX, double cameraY, double cameraZ) {
        if (!Backend.canUseInstancing()) return;

        ClientWorld world = Minecraft.getInstance().world;
        KineticRenderer kineticRenderer = CreateClient.kineticRenderer.get(world);

        layer.startDrawing();

        kineticRenderer.render(layer, viewProjection, cameraX, cameraY, cameraZ);

        layer.endDrawing();
    }

    // copied from GameRenderer.renderWorld
    public static Matrix4f getProjectionMatrix() {
        if (projectionMatrixThisFrame != null) return projectionMatrixThisFrame;

        float partialTicks = AnimationTickHolder.getPartialTicks();
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        ClientPlayerEntity player = mc.player;

        MatrixStack matrixstack = new MatrixStack();
		matrixstack.peek()
			.getModel()
			.multiply(gameRenderer.getBasicProjectionMatrix(gameRenderer.getActiveRenderInfo(), partialTicks, true));
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
        gameRenderer.loadProjectionMatrix(matrix4f);

        projectionMatrixThisFrame = matrix4f;
        return projectionMatrixThisFrame;
    }
}
