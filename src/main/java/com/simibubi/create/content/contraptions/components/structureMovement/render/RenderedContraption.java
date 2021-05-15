package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;
import com.jozufozu.flywheel.backend.light.GridAlignedBB;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

public class RenderedContraption extends ContraptionWorldHolder {
    private final ContraptionLighter<?> lighter;
    public final ContraptionKineticRenderer kinetics;

    private final Map<RenderType, ContraptionModel> renderLayers = new HashMap<>();

    private Matrix4f model;
    private AxisAlignedBB lightBox;

    public RenderedContraption(World world, PlacementSimulationWorld renderWorld, Contraption contraption) {
        super(contraption, renderWorld);
        this.lighter = contraption.makeLighter();
        this.kinetics = new ContraptionKineticRenderer(this);

        buildLayers();
        if (Backend.canUseInstancing()) {
            buildInstancedTiles();
            buildActors();
        }
    }

    public ContraptionLighter<?> getLighter() {
        return lighter;
    }

    public void doRenderLayer(RenderType layer, ContraptionProgram shader) {
        ContraptionModel structure = renderLayers.get(layer);
        if (structure != null) {
            setup(shader);
            structure.render();
            teardown();
        }
    }

    public void beginFrame(ActiveRenderInfo info, double camX, double camY, double camZ) {
        kinetics.beginFrame(info);

        AbstractContraptionEntity entity = contraption.entity;
        float pt = AnimationTickHolder.getPartialTicks();

        MatrixStack stack = new MatrixStack();

        double x = MathHelper.lerp(pt, entity.lastTickPosX, entity.getX()) - camX;
        double y = MathHelper.lerp(pt, entity.lastTickPosY, entity.getY()) - camY;
        double z = MathHelper.lerp(pt, entity.lastTickPosZ, entity.getZ()) - camZ;
        stack.translate(x, y, z);

        entity.doLocalTransforms(pt, new MatrixStack[]{ stack });

        model = stack.peek().getModel();

        AxisAlignedBB lightBox = GridAlignedBB.toAABB(lighter.lightVolume.getTextureVolume());

        this.lightBox = lightBox.offset(-camX, -camY, -camZ);
    }

    void setup(ContraptionProgram shader) {
        if (model == null || lightBox == null) return;
        shader.bind(model, lightBox);
        lighter.lightVolume.bind();
    }

    void teardown() {
        lighter.lightVolume.unbind();
    }

    void invalidate() {
        for (ContraptionModel buffer : renderLayers.values()) {
            buffer.delete();
        }
        renderLayers.clear();

        lighter.lightVolume.delete();

        kinetics.invalidate();
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
        contraption.getActors().forEach(kinetics::createActor);
    }

    private static ContraptionModel buildStructureModel(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
        BufferBuilder builder = ContraptionRenderDispatcher.buildStructure(renderWorld, c, layer);
        return new ContraptionModel(builder);
    }
}
