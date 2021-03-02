package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.logistics.block.FlapData;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;

public class FunnelInstance extends TileEntityInstance<FunnelTileEntity> {
    public static void register(TileEntityType<? extends FunnelTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, FunnelInstance::new));
    }

    private ArrayList<InstanceKey<FlapData>> flaps;

    public FunnelInstance(InstancedTileRenderer<?> modelManager, FunnelTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        flaps = new ArrayList<>(4);

        if (!tile.hasFlap()) return;

        AllBlockPartials flapPartial = (lastState.getBlock() instanceof FunnelBlock ? AllBlockPartials.FUNNEL_FLAP
                : AllBlockPartials.BELT_FUNNEL_FLAP);
        InstancedModel<FlapData> model = modelManager.getMaterial(KineticRenderMaterials.FLAPS)
                                                     .getModel(flapPartial, lastState);

        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        Direction direction = FunnelBlock.getFunnelFacing(lastState);

        float flapness = tile.flap.get(AnimationTickHolder.getPartialTicks());
        float horizontalAngle = direction.getOpposite().getHorizontalAngle();

        for (int segment = 0; segment <= 3; segment++) {
            float intensity = segment == 3 ? 1.5f : segment + 1;
            float segmentOffset = -3 / 16f * segment;

            flaps.add(model.setupInstance(flapData -> flapData.setPosition(pos)
                                                              .setSegmentOffset(segmentOffset, 0, -tile.getFlapOffset())
                                                              .setBlockLight(blockLight)
                                                              .setSkyLight(skyLight)
                                                              .setHorizontalAngle(horizontalAngle)
                                                              .setFlapness(flapness)
                                                              .setFlapScale(-1)
                                                              .setPivotVoxelSpace(0, 10, 9.5f)
                                                              .setIntensity(intensity)));
        }
    }

    @Override
    public void tick() {
        if (flaps == null) return;

        float flapness = tile.flap.get(AnimationTickHolder.getPartialTicks());

        for (InstanceKey<FlapData> key : flaps) {
            key.modifyInstance(data -> data.setFlapness(flapness));
        }
    }

    @Override
    public void updateLight() {
        if (flaps == null) return;

        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        for (InstanceKey<FlapData> it : flaps) {
            it.modifyInstance(data -> data.setBlockLight(blockLight)
                                          .setSkyLight(skyLight));
       }
    }

    @Override
    public void remove() {
        if (flaps == null) return;

        flaps.forEach(InstanceKey::delete);
    }
}
