package com.simibubi.create.content.logistics.block.funnel;

import java.util.ArrayList;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.logistics.block.FlapData;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.TileEntityInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Direction;
import net.minecraft.world.LightType;

public class FunnelInstance extends TileEntityInstance<FunnelTileEntity> implements IDynamicInstance {

    private final ArrayList<FlapData> flaps;

    public FunnelInstance(InstancedTileRenderer<?> modelManager, FunnelTileEntity tile) {
        super(modelManager, tile);

        flaps = new ArrayList<>(4);

        if (!tile.hasFlap()) return;

        AllBlockPartials flapPartial = (blockState.getBlock() instanceof FunnelBlock ? AllBlockPartials.FUNNEL_FLAP
                : AllBlockPartials.BELT_FUNNEL_FLAP);
        InstancedModel<FlapData> model = modelManager.getMaterial(KineticRenderMaterials.FLAPS)
                                                     .getModel(flapPartial, blockState);

        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        Direction direction = FunnelBlock.getFunnelFacing(blockState);

        float flapness = tile.flap.get(AnimationTickHolder.getPartialTicks());
        float horizontalAngle = direction.getOpposite().getHorizontalAngle();

        for (int segment = 0; segment <= 3; segment++) {
            float intensity = segment == 3 ? 1.5f : segment + 1;
            float segmentOffset = -3 / 16f * segment;

            FlapData key = model.createInstance();

            key.setPosition(pos)
               .setSegmentOffset(segmentOffset, 0, -tile.getFlapOffset())
               .setBlockLight(blockLight)
               .setSkyLight(skyLight)
               .setHorizontalAngle(horizontalAngle)
               .setFlapness(flapness)
               .setFlapScale(-1)
               .setPivotVoxelSpace(0, 10, 9.5f)
               .setIntensity(intensity);

            flaps.add(key);
        }
    }

    @Override
    public void beginFrame() {
        if (flaps == null) return;

        float flapness = tile.flap.get(AnimationTickHolder.getPartialTicks());

        for (FlapData flap : flaps) {
            flap.setFlapness(flapness);
        }
    }

    @Override
    public void updateLight() {
        if (flaps != null)
            relight(pos, flaps.stream());
    }

    @Override
    public void remove() {
        if (flaps == null) return;

        flaps.forEach(InstanceData::delete);
    }
}
