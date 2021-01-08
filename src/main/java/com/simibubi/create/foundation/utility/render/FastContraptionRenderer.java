package com.simibubi.create.foundation.utility.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRenderer;
import com.simibubi.create.foundation.utility.render.shader.Shader;
import com.simibubi.create.foundation.utility.render.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class FastContraptionRenderer extends ContraptionRenderer {

    private static final Cache<Integer, FastContraptionRenderer> renderers = CacheBuilder.newBuilder().build();

    private ArrayList<ContraptionBuffer> renderLayers = new ArrayList<>();

    private ContraptionLighter lighter;

    private Contraption c;

    private Vec3d renderPos;
    private Vec3d renderRot;

    public FastContraptionRenderer(World world, Contraption c) {
        this.c = c;
        this.lighter = new ContraptionLighter(c);

        buildLayers();
    }

    private void setRenderSettings(Vec3d position, Vec3d rotation) {
        renderPos = position;
        renderRot = rotation;
    }

    private void render(int shader) {
//        GL13.glActiveTexture(GL40.GL_TEXTURE2);
//        lighter.use();

        int cSize = GlStateManager.getUniformLocation(shader, "cSize");
        int cPos = GlStateManager.getUniformLocation(shader, "cPos");
        int cRot = GlStateManager.getUniformLocation(shader, "cRot");

        FloatBuffer buf = ShaderHelper.VEC3_BUFFER;

        buf.put(0, (float) c.bounds.getXSize());
        buf.put(1, (float) c.bounds.getYSize());
        buf.put(2, (float) c.bounds.getZSize());
        buf.rewind();
        GlStateManager.uniform3(cSize, buf);

        buf.put(0, (float) renderPos.x);
        buf.put(1, (float) renderPos.y);
        buf.put(2, (float) renderPos.z);
        buf.rewind();
        GlStateManager.uniform3(cPos, buf);

        buf.put(0, (float) renderRot.x);
        buf.put(1, (float) renderRot.y);
        buf.put(2, (float) renderRot.z);
        buf.rewind();
        GlStateManager.uniform3(cRot, buf);

        for (ContraptionBuffer layer : renderLayers) {
            layer.render();
        }
    }

    private void buildLayers() {
        invalidate();

        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (int i = 0; i < blockLayers.size(); i++) {
            RenderType layer = blockLayers.get(i);
            renderLayers.add(buildStructureBuffer(c, layer));
        }
    }

    private void invalidate() {
        for (ContraptionBuffer buffer : renderLayers) {
            buffer.invalidate();
        }

        renderLayers.clear();
    }

    public static void markForRendering(World world, Contraption c, Vec3d position, Vec3d rotation) {
        getRenderer(world, c).setRenderSettings(position, rotation);
    }

    private static FastContraptionRenderer getRenderer(World world, Contraption c) {
        try {
            return renderers.get(c.entity.getEntityId(), () -> new FastContraptionRenderer(world, c));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void renderAll(RenderWorldLastEvent event) {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        CreateClient.kineticRenderer.setup(gameRenderer);
        GlStateManager.enableCull();

        ShaderHelper.useShader(Shader.CONTRAPTION_STRUCTURE, ShaderHelper.getViewProjectionCallback(event));
        int shader = ShaderHelper.getShaderHandle(Shader.CONTRAPTION_STRUCTURE);

        ArrayList<Integer> toRemove = new ArrayList<>();

        for (FastContraptionRenderer renderer : renderers.asMap().values()) {
            if (renderer.c.entity.isAlive())
                renderer.render(shader);
            else
                toRemove.add(renderer.c.entity.getEntityId());
        }

        ShaderHelper.releaseShader();

        CreateClient.kineticRenderer.teardown();

        renderers.invalidateAll(toRemove);
    }

    public static void invalidateAll() {
        for (FastContraptionRenderer renderer : renderers.asMap().values()) {
            renderer.invalidate();
        }

        renderers.invalidateAll();
    }

    private static ContraptionBuffer buildStructureBuffer(Contraption c, RenderType layer) {
        BufferBuilder builder = buildStructure(c, layer);
        return new ContraptionBuffer(builder);
    }
}
