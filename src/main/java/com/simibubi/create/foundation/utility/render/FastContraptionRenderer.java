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
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class FastContraptionRenderer extends ContraptionRenderer {

    private static final HashMap<Integer, FastContraptionRenderer> renderers = new HashMap<>();

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

    public static void tick() {
        if (Minecraft.getInstance().isGamePaused()) return;

        CreateClient.kineticRenderer.enqueue(() -> {
            for (FastContraptionRenderer renderer : renderers.values()) {
                renderer.lighter.tick(renderer.c);
            }
        });
    }

    private void setRenderSettings(Vec3d position, Vec3d rotation) {
        renderPos = position;
        renderRot = rotation;
    }

    private void render(int shader) {
        lighter.use();

        FloatBuffer buf = ShaderHelper.VEC3_BUFFER;

        int lightBoxSize = GlStateManager.getUniformLocation(shader, "lightBoxSize");
        buf.put(0, (float) lighter.getSizeX());
        buf.put(1, (float) lighter.getSizeY());
        buf.put(2, (float) lighter.getSizeZ());
        buf.rewind();
        GlStateManager.uniform3(lightBoxSize, buf);

        int lightBoxMin = GlStateManager.getUniformLocation(shader, "lightBoxMin");
        buf.put(0, (float) lighter.getMinX());
        buf.put(1, (float) lighter.getMinY());
        buf.put(2, (float) lighter.getMinZ());
        buf.rewind();
        GlStateManager.uniform3(lightBoxMin, buf);

        int cPos = GlStateManager.getUniformLocation(shader, "cPos");
        buf.put(0, (float) renderPos.x);
        buf.put(1, (float) renderPos.y);
        buf.put(2, (float) renderPos.z);
        buf.rewind();
        GlStateManager.uniform3(cPos, buf);

        int cRot = GlStateManager.getUniformLocation(shader, "cRot");
        buf.put(0, (float) renderRot.x);
        buf.put(1, (float) renderRot.y);
        buf.put(2, (float) renderRot.z);
        buf.rewind();
        GlStateManager.uniform3(cRot, buf);

        for (ContraptionBuffer layer : renderLayers) {
            layer.render();
        }

        lighter.release();
    }

    private void buildLayers() {
        invalidate();

        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (RenderType layer : blockLayers) {
            renderLayers.add(buildStructureBuffer(c, layer));
        }
    }

    private void invalidate() {
        for (ContraptionBuffer buffer : renderLayers) {
            buffer.invalidate();
        }
        lighter.delete();

        renderLayers.clear();
    }

    public static void markForRendering(World world, Contraption c, Vec3d position, Vec3d rotation) {
        getRenderer(world, c).setRenderSettings(position, rotation);
    }

    private static FastContraptionRenderer getRenderer(World world, Contraption c) {
        FastContraptionRenderer renderer;
        int entityId = c.entity.getEntityId();
        if (renderers.containsKey(entityId)) {
            renderer = renderers.get(entityId);
        } else {
            renderer = new FastContraptionRenderer(world, c);
            renderers.put(entityId, renderer);
        }

        return renderer;
    }

    public static void renderAll(RenderWorldLastEvent event) {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        CreateClient.kineticRenderer.setup(gameRenderer);
        GlStateManager.enableCull();

        ShaderHelper.useShader(Shader.CONTRAPTION_STRUCTURE, ShaderHelper.getViewProjectionCallback(event));
        int shader = ShaderHelper.getShaderHandle(Shader.CONTRAPTION_STRUCTURE);

        ArrayList<Integer> toRemove = new ArrayList<>();

        for (FastContraptionRenderer renderer : renderers.values()) {
            if (renderer.c.entity.isAlive())
                renderer.render(shader);
            else
                toRemove.add(renderer.c.entity.getEntityId());
        }

        ShaderHelper.releaseShader();

        CreateClient.kineticRenderer.teardown();

        for (Integer id : toRemove) {
            renderers.remove(id);
        }
    }

    public static void invalidateAll() {
        for (FastContraptionRenderer renderer : renderers.values()) {
            renderer.invalidate();
        }

        renderers.clear();
    }

    private static ContraptionBuffer buildStructureBuffer(Contraption c, RenderType layer) {
        BufferBuilder builder = buildStructure(c, layer);
        return new ContraptionBuffer(builder);
    }
}
