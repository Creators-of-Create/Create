package com.simibubi.create.content.schematics.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelInstance;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class SchematicannonInstance extends TileEntityInstance<SchematicannonTileEntity> implements ITickableInstance {
    public static void register(TileEntityType<? extends SchematicannonTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, SchematicannonInstance::new));
    }

    private InstanceKey<ModelData> connector;
    private InstanceKey<ModelData> pipe;

    public SchematicannonInstance(InstancedTileRenderer<?> modelManager, SchematicannonTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {

        RenderMaterial<?, InstancedModel<ModelData>> mat = modelManager.getMaterial(RenderMaterials.MODELS);

        connector = mat.getModel(AllBlockPartials.SCHEMATICANNON_CONNECTOR, lastState).createInstance();
        pipe = mat.getModel(AllBlockPartials.SCHEMATICANNON_PIPE, lastState).createInstance();

        updateLight();
    }

    @Override
    public void tick() {
        float partialTicks = AnimationTickHolder.getPartialTicks();

        double[] cannonAngles = SchematicannonRenderer.getCannonAngles(tile, pos, partialTicks);

        double pitch = cannonAngles[0];
        double yaw = cannonAngles[1];

        double recoil = SchematicannonRenderer.getRecoil(tile, partialTicks);

        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(getFloatingPos());

        ms.push();
        msr.centre();
        msr.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
        msr.unCentre();
        connector.getInstance().setTransform(ms);
        ms.pop();

        msr.translate(.5f, 15 / 16f, .5f);
        msr.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
        msr.rotate(Direction.SOUTH, (float) (pitch / 180 * Math.PI));
        msr.translate(-.5f, -15 / 16f, -.5f);
        msr.translate(0, -recoil / 100, 0);

        pipe.getInstance().setTransformNoCopy(ms);
    }

    @Override
    public void remove() {
        connector.delete();
        pipe.delete();
    }

    @Override
    public void updateLight() {
        int block = world.getLightLevel(LightType.BLOCK, pos);
        int sky = world.getLightLevel(LightType.SKY, pos);

        connector.getInstance()
                 .setBlockLight(block)
                 .setSkyLight(sky);

        pipe.getInstance()
            .setBlockLight(block)
            .setSkyLight(sky);
    }
}
