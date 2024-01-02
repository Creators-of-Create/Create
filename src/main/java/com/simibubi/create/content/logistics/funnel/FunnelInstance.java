package com.simibubi.create.content.logistics.funnel;

import java.util.ArrayList;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicVisual;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.flwdata.FlapData;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

public class FunnelInstance extends BlockEntityInstance<FunnelBlockEntity> implements DynamicVisual {

    private final ArrayList<FlapData> flaps;

    public FunnelInstance(VisualizationContext materialManager, FunnelBlockEntity blockEntity) {
        super(materialManager, blockEntity);

        flaps = new ArrayList<>(4);

        if (!blockEntity.hasFlap()) return;

		PartialModel flapPartial = (blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP
				: AllPartialModels.BELT_FUNNEL_FLAP);
        Instancer<FlapData> model = instancerProvider.instancer(AllInstanceTypes.FLAPS, Models.partial(flapPartial), RenderStage.AFTER_BLOCK_ENTITIES);

        int blockLight = world.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = world.getBrightness(LightLayer.SKY, pos);

        Direction direction = FunnelBlock.getFunnelFacing(blockState);

        float flapness = blockEntity.flap.getValue(AnimationTickHolder.getPartialTicks());
        float horizontalAngle = direction.getOpposite().toYRot();

        for (int segment = 0; segment <= 3; segment++) {
            float intensity = segment == 3 ? 1.5f : segment + 1;
            float segmentOffset = -3.05f / 16f * segment + 0.075f / 16f;

            FlapData key = model.createInstance();

            key.setPosition(getVisualPosition())
               .setSegmentOffset(segmentOffset, 0, -blockEntity.getFlapOffset())
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

        float flapness = blockEntity.flap.getValue(AnimationTickHolder.getPartialTicks());

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

        flaps.forEach(AbstractInstance::delete);
    }
}
