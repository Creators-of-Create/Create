package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContraptionRenderDispatcher {
    public static final HashMap<Integer, RenderedContraption> renderers = new HashMap<>();

    public static void notifyLightUpdate(ILightReader world, LightType type, SectionPos pos) {
        for (RenderedContraption renderer : renderers.values()) {
            renderer.getLighter().lightVolume.notifyLightUpdate(world, type, pos);
        }
    }

    public static void renderTick(TickEvent.RenderTickEvent event) {
        ClientWorld world = Minecraft.getInstance().world;
        if (event.phase == TickEvent.Phase.START && world != null) {
            Map<Integer, WeakReference<AbstractContraptionEntity>> map = ContraptionHandler.loadedContraptions.get(world);

            for (WeakReference<AbstractContraptionEntity> weakReference : map.values()) {
                AbstractContraptionEntity entity = weakReference.get();

                EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();

                EntityRenderer<?> renderer = renderManager.getRenderer(entity);

                if (renderer instanceof AbstractContraptionEntityRenderer) {
                    updateTransform(entity, (AbstractContraptionEntityRenderer<? super AbstractContraptionEntity>) renderer);
                }
            }
        }
    }

    public static void renderTileEntities(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
                                          IRenderTypeBuffer buffer) {
        RenderedContraption renderer = getRenderer(world, c);

        TileEntityRenderHelper.renderTileEntities(world, renderer.renderWorld, c.specialRenderedTileEntities, ms, msLocal, buffer);
    }

    private static <C extends AbstractContraptionEntity> void updateTransform(C c, AbstractContraptionEntityRenderer<C> entityRenderer) {
        MatrixStack stack = entityRenderer.makeTransformMatrix(c, AnimationTickHolder.getPartialTicks());

        Contraption c1 = c.getContraption();
        getRenderer(c1.entity.world, c1).setRenderSettings(stack.peek().getModel());
    }

    public static void tick() {
        for (RenderedContraption contraption : renderers.values()) {
            contraption.getLighter().tick(contraption);
        }
    }

    private static RenderedContraption getRenderer(World world, Contraption c) {
        RenderedContraption renderer;
        int entityId = c.entity.getEntityId();
        if (renderers.containsKey(entityId)) {
            renderer = renderers.get(entityId);
        } else {
            renderer = new RenderedContraption(world, c);
            renderers.put(entityId, renderer);
        }

        return renderer;
    }

    public static void renderLayer(RenderType layer, Matrix4f viewProjection, float camX, float camY, float camZ) {
        removeDeadContraptions();

        if (renderers.isEmpty()) return;

        layer.startDrawing();
        GL11.glEnable(GL13.GL_TEXTURE_3D);
        GL13.glActiveTexture(GL40.GL_TEXTURE4); // the shaders expect light volumes to be in texture 4

        if (Backend.canUseVBOs()) {
            ContraptionProgram structureShader = Backend.getProgram(AllProgramSpecs.CONTRAPTION_STRUCTURE);
            structureShader.bind(viewProjection, camX, camY, camZ, FastRenderDispatcher.getDebugMode());
            for (RenderedContraption renderer : renderers.values()) {
                renderer.doRenderLayer(layer, structureShader);
            }
        }

        if (Backend.canUseInstancing()) {
            for (RenderedContraption renderer : renderers.values()) {
                renderer.kinetics.render(layer, viewProjection, camX, camY, camZ, renderer::setup);
                renderer.teardown();
            }
        }

        layer.endDrawing();
        GL11.glDisable(GL13.GL_TEXTURE_3D);
        GL13.glActiveTexture(GL40.GL_TEXTURE0);
    }

    public static void removeDeadContraptions() {
        ArrayList<Integer> toRemove = new ArrayList<>();

        for (RenderedContraption renderer : renderers.values()) {
            if (renderer.isDead()) {
                toRemove.add(renderer.getEntityId());
                renderer.invalidate();
            }
        }


        for (Integer id : toRemove) {
            renderers.remove(id);
        }
    }

    public static void invalidateAll() {
        for (RenderedContraption renderer : renderers.values()) {
            renderer.invalidate();
        }

        renderers.clear();
    }
}
