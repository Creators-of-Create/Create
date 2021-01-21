package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.render.light.LightVolume;
import com.simibubi.create.foundation.render.shader.Shader;
import com.simibubi.create.foundation.render.shader.ShaderCallback;
import com.simibubi.create.foundation.render.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ContraptionRenderDispatcher {
    public static final HashMap<Integer, RenderedContraption> renderers = new HashMap<>();

    public static void markForRendering(World world, Contraption c, MatrixStack model) {
        getRenderer(world, c).setRenderSettings(model.peek().getModel());
    }

    public static void notifyLightUpdate(ILightReader world, LightType type, SectionPos pos) {
        for (RenderedContraption renderer : renderers.values()) {
            renderer.getLighter().lightVolume.notifyLightUpdate(world, type, pos);
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

    public static void renderLayer(RenderType renderType, Matrix4f projectionMat, Matrix4f viewMat) {
        removeDeadContraptions();

        if (renderers.isEmpty()) return;

        renderType.startDrawing();
        GL11.glEnable(GL13.GL_TEXTURE_3D);
        GL13.glActiveTexture(GL40.GL_TEXTURE4); // the shaders expect light volumes to be in texture 4

        ShaderCallback callback = ShaderHelper.getViewProjectionCallback(projectionMat, viewMat);

        int structureShader = ShaderHelper.useShader(Shader.CONTRAPTION_STRUCTURE, callback);
        for (RenderedContraption renderer : renderers.values()) {
            renderer.doRenderLayer(renderType, structureShader);
        }

        if (renderType == FastKineticRenderer.getKineticRenderLayer()) {
            int rotatingShader = ShaderHelper.useShader(Shader.CONTRAPTION_ROTATING, callback);
            for (RenderedContraption renderer : renderers.values()) {
                renderer.setup(rotatingShader);
                renderer.kinetics.renderRotating();
                renderer.teardown();
            }

            int beltShader = ShaderHelper.useShader(Shader.CONTRAPTION_BELT, callback);
            for (RenderedContraption renderer : renderers.values()) {
                renderer.setup(beltShader);
                renderer.kinetics.renderBelts();
                renderer.teardown();
            }
        }

        ShaderHelper.releaseShader();


        renderType.endDrawing();
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
