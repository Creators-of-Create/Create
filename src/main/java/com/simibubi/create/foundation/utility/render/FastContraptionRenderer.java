package com.simibubi.create.foundation.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRenderer;
import com.simibubi.create.foundation.utility.render.instancing.IInstanceRendered;
import com.simibubi.create.foundation.utility.render.instancing.IInstancedTileEntityRenderer;
import com.simibubi.create.foundation.utility.render.shader.Shader;
import com.simibubi.create.foundation.utility.render.shader.ShaderCallback;
import com.simibubi.create.foundation.utility.render.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FastContraptionRenderer extends ContraptionRenderer {

    private static final HashMap<Integer, FastContraptionRenderer> renderers = new HashMap<>();

    private ArrayList<ContraptionBuffer> renderLayers = new ArrayList<>();

    private ContraptionLighter lighter;

    public final FastKineticRenderer kinetics;

    private Contraption c;

    private Vec3d renderPos;
    private Vec3d renderRot;

    public FastContraptionRenderer(World world, Contraption c) {
        this.c = c;
        this.lighter = new ContraptionLighter(c);
        this.kinetics = new FastKineticRenderer();

        buildLayers(c);
        buildInstancedTiles(c);
    }

    private void buildLayers(Contraption c) {
        for (ContraptionBuffer buffer : renderLayers) {
            buffer.delete();
        }

        renderLayers.clear();

        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (RenderType layer : blockLayers) {
            renderLayers.add(buildStructureBuffer(c, layer));
        }
    }

    private void buildInstancedTiles(Contraption c) {
        List<TileEntity> tileEntities = c.renderedTileEntities;
        if (!tileEntities.isEmpty()) {
            for (TileEntity te : tileEntities) {
                if (te instanceof IInstanceRendered) {
                    TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);

                    if (renderer instanceof IInstancedTileEntityRenderer) {
                        kinetics.addInstancedData(this, te, (IInstancedTileEntityRenderer<? super TileEntity>) renderer);
                    }
                }
            }
        }

        kinetics.markAllDirty();
    }

    public static void tick() {
        if (Minecraft.getInstance().isGamePaused()) return;

        for (FastContraptionRenderer renderer : renderers.values()) {
            renderer.lighter.update(renderer.c);
        }
    }

    private void setRenderSettings(Vec3d position, Vec3d rotation) {
        renderPos = position;
        renderRot = rotation;
    }

    private void setup(int shader) {
        setupShaderUniforms(shader);
        lighter.use();
    }

    private void teardown() {
        lighter.release();
    }

    private void setupShaderUniforms(int shader) {
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
    }

    private void invalidate() {
        for (ContraptionBuffer buffer : renderLayers) {
            buffer.delete();
        }

        lighter.delete();

        kinetics.invalidate();

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

    public static void renderAll(Matrix4f projectionMat, Matrix4f viewMat) {
        removeDeadContraptions();

        if (renderers.isEmpty()) return;

        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        FastKineticRenderer.setup(gameRenderer);
        GL11.glEnable(GL13.GL_TEXTURE_3D);
        GL13.glActiveTexture(GL40.GL_TEXTURE4);

        ShaderCallback callback = ShaderHelper.getViewProjectionCallback(projectionMat, viewMat);

        int structureShader = ShaderHelper.useShader(Shader.CONTRAPTION_STRUCTURE, callback);
        for (FastContraptionRenderer renderer : renderers.values()) {
            renderer.setup(structureShader);
            for (ContraptionBuffer layer : renderer.renderLayers) {
                layer.render();
            }
            renderer.teardown();
        }

        int rotatingShader = ShaderHelper.useShader(Shader.CONTRAPTION_ROTATING, callback);
        for (FastContraptionRenderer renderer : renderers.values()) {
            renderer.setup(rotatingShader);
            renderer.kinetics.renderRotating();
            renderer.teardown();
        }

        int beltShader = ShaderHelper.useShader(Shader.CONTRAPTION_BELT, callback);
        for (FastContraptionRenderer renderer : renderers.values()) {
            renderer.setup(beltShader);
            renderer.kinetics.renderBelts();
            renderer.teardown();
        }

        ShaderHelper.releaseShader();

        GL11.glDisable(GL13.GL_TEXTURE_3D);
        FastKineticRenderer.teardown();
    }

    public static void removeDeadContraptions() {
        ArrayList<Integer> toRemove = new ArrayList<>();

        for (FastContraptionRenderer renderer : renderers.values()) {
            if (!renderer.c.entity.isAlive()) {
                toRemove.add(renderer.c.entity.getEntityId());
                renderer.invalidate();
            }
        }

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
