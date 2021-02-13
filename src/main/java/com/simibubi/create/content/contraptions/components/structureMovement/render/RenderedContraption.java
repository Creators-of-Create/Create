package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.content.contraptions.components.actors.ContraptionActorData;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RenderedContraption {
    private HashMap<RenderType, ContraptionModel> renderLayers = new HashMap<>();

    public final PlacementSimulationWorld renderWorld;

    private final ContraptionLighter<?> lighter;

    public final ContraptionKineticRenderer kinetics;

    public Contraption contraption;

    private Matrix4f model;

    public RenderedContraption(World world, Contraption contraption) {
        this.contraption = contraption;
        this.lighter = contraption.makeLighter();
        this.kinetics = new ContraptionKineticRenderer();
        this.renderWorld = setupRenderWorld(world, contraption);

        buildLayers();
        if (Backend.canUseInstancing()) {
            buildInstancedTiles();
            buildActors();
        }
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

    public RenderMaterial<?, InstancedModel<ContraptionActorData>> getActorMaterial() {
        return kinetics.getMaterial(KineticRenderMaterials.ACTORS);
    }

    public void doRenderLayer(RenderType layer, ContraptionProgram shader) {
        ContraptionModel buffer = renderLayers.get(layer);
        if (buffer != null) {
            setup(shader);
            buffer.render();
            teardown();
        }
    }

    private void buildLayers() {
        for (ContraptionModel buffer : renderLayers.values()) {
            buffer.delete();
        }

        renderLayers.clear();

        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (RenderType layer : blockLayers) {
            renderLayers.put(layer, buildStructureModel(renderWorld, contraption, layer));
        }
    }

    private void buildInstancedTiles() {
        Collection<TileEntity> tileEntities = contraption.maybeInstancedTileEntities;
        if (!tileEntities.isEmpty()) {
            for (TileEntity te : tileEntities) {
                if (te instanceof IInstanceRendered) {
                    World world = te.getWorld();
                    BlockPos pos = te.getPos();
                    te.setLocation(renderWorld, pos);
                    kinetics.add(te);
                    te.setLocation(world, pos);
                }
            }
        }
    }

    private void buildActors() {
        List<MutablePair<Template.BlockInfo, MovementContext>> actors = contraption.getActors();

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

    void setup(ContraptionProgram shader) {
        if (model == null) return;
        shader.bind(model, lighter.lightVolume.getTextureVolume());
        lighter.lightVolume.use();
    }

    void teardown() {
        lighter.lightVolume.release();
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

    public static PlacementSimulationWorld setupRenderWorld(World world, Contraption c) {
        PlacementSimulationWorld renderWorld = new PlacementSimulationWorld(world);

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
