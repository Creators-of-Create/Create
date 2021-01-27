package com.simibubi.create.foundation.render.contraption;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.gl.shader.ShaderHelper;
import com.simibubi.create.foundation.render.instancing.IInstanceRendered;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.KineticRenderMaterials;
import com.simibubi.create.foundation.render.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.instancing.actors.StaticRotatingActorData;
import com.simibubi.create.foundation.render.light.ContraptionLighter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import org.apache.commons.lang3.tuple.MutablePair;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RenderedContraption {
    private HashMap<RenderType, ContraptionModel> renderLayers = new HashMap<>();

    private final ContraptionLighter<?> lighter;

    public final ContraptionKineticRenderer kinetics;

    public Contraption contraption;

    private Matrix4f model;

    public RenderedContraption(World world, Contraption contraption) {
        this.contraption = contraption;
        this.lighter = contraption.makeLighter();
        this.kinetics = new ContraptionKineticRenderer();

        buildLayers(contraption);
        buildInstancedTiles(contraption);
        buildActors(contraption);
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

    public RenderMaterial<InstancedModel<StaticRotatingActorData>> getActorMaterial() {
        return kinetics.get(KineticRenderMaterials.ACTORS);
    }

    public void doRenderLayer(RenderType layer, int shader) {
        ContraptionModel buffer = renderLayers.get(layer);
        if (buffer != null) {
            setup(shader);
            buffer.render();
            teardown();
        }
    }

    private void buildLayers(Contraption c) {
        for (ContraptionModel buffer : renderLayers.values()) {
            buffer.delete();
        }

        renderLayers.clear();

        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (RenderType layer : blockLayers) {
            renderLayers.put(layer, buildStructureBuffer(c, layer));
        }
    }

    private void buildInstancedTiles(Contraption c) {
        Collection<TileEntity> tileEntities = c.maybeInstancedTileEntities;
        if (!tileEntities.isEmpty()) {
            for (TileEntity te : tileEntities) {
                if (te instanceof IInstanceRendered) {
                    kinetics.getRenderer(te); // this is enough to instantiate the model instance
                }
            }
        }
    }

    private void buildActors(Contraption c) {
        List<MutablePair<Template.BlockInfo, MovementContext>> actors = c.getActors();

        for (MutablePair<Template.BlockInfo, MovementContext> actor : actors) {
            Template.BlockInfo blockInfo = actor.left;
            MovementContext context = actor.right;

            MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);

            if (movementBehaviour != null) {
                movementBehaviour.addInstance(this, context);
            }
        }
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
        for (ContraptionModel buffer : renderLayers.values()) {
            buffer.delete();
        }
        renderLayers.clear();

        lighter.lightVolume.delete();

        kinetics.invalidate();
    }

    private static ContraptionModel buildStructureBuffer(Contraption c, RenderType layer) {
        BufferBuilder builder = ContraptionRenderer.buildStructure(c, layer);
        return new ContraptionModel(builder);
    }
}
