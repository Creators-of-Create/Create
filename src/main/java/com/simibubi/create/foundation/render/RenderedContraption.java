package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRenderer;
import com.simibubi.create.foundation.render.instancing.IInstanceRendered;
import com.simibubi.create.foundation.render.instancing.IInstancedTileEntityRenderer;
import com.simibubi.create.foundation.render.light.ContraptionLighter;
import com.simibubi.create.foundation.render.light.LightVolume;
import com.simibubi.create.foundation.render.shader.ShaderHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RenderedContraption {
    private HashMap<RenderType, ContraptionBuffer> renderLayers = new HashMap<>();

    private final ContraptionLighter<?> lighter;

    public final FastKineticRenderer kinetics;

    private Contraption contraption;

    private Matrix4f model;

    public RenderedContraption(World world, Contraption contraption) {
        this.contraption = contraption;
        this.lighter = contraption.makeLighter();
        this.kinetics = new FastKineticRenderer();

        buildLayers(contraption);
        buildInstancedTiles(contraption);
    }

    public int getEntityId() {
        return contraption.entity.getEntityId();
    }

    public boolean isDead() {
        return !contraption.entity.isAlive();
    }

    public ContraptionLighter<?> getLighter() {
        return lighter;
    }

    public void doRenderLayer(RenderType layer, int shader) {
        ContraptionBuffer buffer = renderLayers.get(layer);
        if (buffer != null) {
            setup(shader);
            buffer.render();
            teardown();
        }
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

    void setRenderSettings(Matrix4f model) {
        this.model = model;
    }

    void setup(int shader) {
        setupShaderUniforms(shader);
        lighter.lightVolume.use();
    }

    void teardown() {
        lighter.lightVolume.release();
    }

    void setupShaderUniforms(int shader) {
        FloatBuffer buf = ShaderHelper.VEC3_BUFFER;

        int lightBoxSize = GlStateManager.getUniformLocation(shader, "lightBoxSize");
        buf.put(0, (float) lighter.lightVolume.getSizeX());
        buf.put(1, (float) lighter.lightVolume.getSizeY());
        buf.put(2, (float) lighter.lightVolume.getSizeZ());
        buf.rewind();
        GlStateManager.uniform3(lightBoxSize, buf);

        int lightBoxMin = GlStateManager.getUniformLocation(shader, "lightBoxMin");
        buf.put(0, (float) lighter.lightVolume.getMinX());
        buf.put(1, (float) lighter.lightVolume.getMinY());
        buf.put(2, (float) lighter.lightVolume.getMinZ());
        buf.rewind();
        GlStateManager.uniform3(lightBoxMin, buf);

        int model = GlStateManager.getUniformLocation(shader, "model");
        this.model.write(ShaderHelper.MATRIX_BUFFER);
        ShaderHelper.MATRIX_BUFFER.rewind();
        GlStateManager.uniformMatrix4(model, false, ShaderHelper.MATRIX_BUFFER);
    }

    void invalidate() {
        for (ContraptionBuffer buffer : renderLayers.values()) {
            buffer.delete();
        }
        renderLayers.clear();

        lighter.lightVolume.delete();

        kinetics.invalidate();
    }

    private static ContraptionBuffer buildStructureBuffer(Contraption c, RenderType layer) {
        BufferBuilder builder = ContraptionRenderer.buildStructure(c, layer);
        return new ContraptionBuffer(builder);
    }
}
