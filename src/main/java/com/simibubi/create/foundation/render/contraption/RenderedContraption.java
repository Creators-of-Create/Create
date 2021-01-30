package com.simibubi.create.foundation.render.contraption;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.gl.shader.ShaderHelper;
import com.simibubi.create.foundation.render.instancing.*;
import com.simibubi.create.foundation.render.instancing.actors.StaticRotatingActorData;
import com.simibubi.create.foundation.render.light.ContraptionLighter;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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

        PlacementSimulationWorld renderWorld = setupRenderWorld(c);
        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (RenderType layer : blockLayers) {
            renderLayers.put(layer, buildStructureModel(renderWorld, c, layer));
        }
    }

    private void buildInstancedTiles(Contraption c) {
        Collection<TileEntity> tileEntities = c.maybeInstancedTileEntities;
        if (!tileEntities.isEmpty()) {
            for (TileEntity te : tileEntities) {
                if (te instanceof IInstanceRendered) {
                    kinetics.getInstance(te); // this is enough to instantiate the model instance
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

    private static ContraptionModel buildStructureModel(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
        BufferBuilder builder = buildStructure(renderWorld, c, layer);
        return new ContraptionModel(builder);
    }

    public static PlacementSimulationWorld setupRenderWorld(Contraption c) {
        PlacementSimulationWorld renderWorld = new PlacementSimulationWorld(Minecraft.getInstance().world);

        renderWorld.setTileEntities(c.presentTileEntities.values());

        for (Template.BlockInfo info : c.getBlocks()
                                        .values())
            renderWorld.setBlockState(info.pos, info.state);

        WorldLightManager lighter = renderWorld.lighter;

        renderWorld.chunkProvider.getLightSources().forEach((pos) -> lighter.func_215573_a(pos, renderWorld.getLightValue(pos)));

        lighter.tick(Integer.MAX_VALUE, true, false);

        return renderWorld;
    }

    public static BufferBuilder buildStructure(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {

        ForgeHooksClient.setRenderLayer(layer);
        MatrixStack ms = new MatrixStack();
        BlockRendererDispatcher dispatcher = Minecraft.getInstance()
                                                      .getBlockRendererDispatcher();
        BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
        Random random = new Random();
        BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (Template.BlockInfo info : c.getBlocks()
                                        .values()) {
            BlockState state = info.state;

            if (state.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED)
                continue;
            if (!RenderTypeLookup.canRenderInLayer(state, layer))
                continue;

            IBakedModel originalModel = dispatcher.getModelForState(state);
            ms.push();
            ms.translate(info.pos.getX(), info.pos.getY(), info.pos.getZ());
            blockRenderer.renderModel(renderWorld, originalModel, state, info.pos, ms, builder, true, random, 42,
                                      OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
            ms.pop();
        }

        builder.finishDrawing();
        return builder;
    }
}
