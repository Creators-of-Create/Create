package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRenderer;
import com.simibubi.create.foundation.render.instancing.IInstanceRendered;
import com.simibubi.create.foundation.render.instancing.IInstancedTileEntityRenderer;
import com.simibubi.create.foundation.render.shader.Shader;
import com.simibubi.create.foundation.render.shader.ShaderCallback;
import com.simibubi.create.foundation.render.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
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

    private HashMap<RenderType, ContraptionBuffer> renderLayers = new HashMap<>();

    private ContraptionLighter lighter;

    public final FastKineticRenderer kinetics;

    private Contraption c;

    private Matrix4f model;

    public FastContraptionRenderer(World world, Contraption c) {
        this.c = c;
        this.lighter = new ContraptionLighter(c);
        this.kinetics = new FastKineticRenderer();

        buildLayers(c);
        buildInstancedTiles(c);
    }

    private void buildLayers(Contraption c) {
        for (ContraptionBuffer buffer : renderLayers.values()) {
            buffer.delete();
        }

        renderLayers.clear();

        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (RenderType layer : blockLayers) {
            renderLayers.put(layer, buildStructureBuffer(c, layer));
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

    private void setRenderSettings(Matrix4f model) {
        this.model = model;
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

        int model = GlStateManager.getUniformLocation(shader, "model");
        this.model.write(ShaderHelper.MATRIX_BUFFER);
        ShaderHelper.MATRIX_BUFFER.rewind();
        GlStateManager.uniformMatrix4(model, false, ShaderHelper.MATRIX_BUFFER);
    }

    private void invalidate() {
        for (ContraptionBuffer buffer : renderLayers.values()) {
            buffer.delete();
        }
        renderLayers.clear();

        lighter.delete();

        kinetics.invalidate();
    }

    public static void markForRendering(World world, Contraption c, MatrixStack model) {
        getRenderer(world, c).setRenderSettings(model.peek().getModel());
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

    public static void renderLayer(RenderType renderType, Matrix4f projectionMat, Matrix4f viewMat) {
        removeDeadContraptions();

        if (renderers.isEmpty()) return;

        FastKineticRenderer.setup(Minecraft.getInstance().gameRenderer);
        GL11.glEnable(GL13.GL_TEXTURE_3D);
        GL13.glActiveTexture(GL40.GL_TEXTURE4); // the shaders expect light volumes to be in texture 4

        ShaderCallback callback = ShaderHelper.getViewProjectionCallback(projectionMat, viewMat);

        int structureShader = ShaderHelper.useShader(Shader.CONTRAPTION_STRUCTURE, callback);
        for (FastContraptionRenderer renderer : renderers.values()) {
            ContraptionBuffer buffer = renderer.renderLayers.get(renderType);
            if (buffer != null) {
                renderer.setup(structureShader);
                buffer.render();
                renderer.teardown();
            }
        }

        if (renderType == FastKineticRenderer.getKineticRenderLayer()) {
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
        }

        ShaderHelper.releaseShader();

        GL11.glDisable(GL13.GL_TEXTURE_3D);
        FastKineticRenderer.teardown();
        GL13.glActiveTexture(GL40.GL_TEXTURE0);
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
