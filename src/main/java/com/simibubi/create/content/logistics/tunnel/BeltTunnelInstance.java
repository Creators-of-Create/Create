package com.simibubi.create.content.logistics.tunnel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.flwdata.FlapData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

public class BeltTunnelInstance extends BlockEntityInstance<BeltTunnelBlockEntity> implements DynamicInstance {

    private final Map<Direction, ArrayList<FlapData>> tunnelFlaps;

    public BeltTunnelInstance(MaterialManager materialManager, BeltTunnelBlockEntity blockEntity) {
        super(materialManager, blockEntity);

        tunnelFlaps = new EnumMap<>(Direction.class);

        Instancer<FlapData> model = materialManager.defaultSolid()
                .material(AllMaterialSpecs.FLAPS)
				.getModel(AllPartialModels.BELT_TUNNEL_FLAP, blockState);

        int blockLight = world.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = world.getBrightness(LightLayer.SKY, pos);

        blockEntity.flaps.forEach((direction, flapValue) -> {

            float flapness = flapValue.getValue(AnimationTickHolder.getPartialTicks());

            float horizontalAngle = direction.getOpposite().toYRot();

            float flapScale = direction.getAxis() == Direction.Axis.X ? 1 : -1;

            ArrayList<FlapData> flaps = new ArrayList<>(4);

            for (int segment = 0; segment <= 3; segment++) {
                float intensity = segment == 3 ? 1.5f : segment + 1;
                float segmentOffset = -3.05f / 16f * segment + 0.075f / 16f;

                FlapData key = model.createInstance();

                key.setPosition(getInstancePosition())
                   .setSegmentOffset(segmentOffset, 0, 0)
                   .setBlockLight(blockLight)
                   .setSkyLight(skyLight)
                   .setHorizontalAngle(horizontalAngle)
                   .setFlapness(flapness)
                   .setFlapScale(flapScale)
                   .setPivotVoxelSpace(0, 10, 1)
                   .setIntensity(intensity);

                flaps.add(key);
            }

            tunnelFlaps.put(direction, flaps);
        });
    }

	@Override
	public boolean shouldReset() {
		return super.shouldReset() || tunnelFlaps.size() != blockEntity.flaps.size();
	}

    @Override
    public void beginFrame() {
        tunnelFlaps.forEach((direction, keys) -> {
            LerpedFloat lerpedFloat = blockEntity.flaps.get(direction);
            if (lerpedFloat == null) 
                return;

            float flapness = lerpedFloat.getValue(AnimationTickHolder.getPartialTicks());
            for (FlapData flap : keys) {
                flap.setFlapness(flapness);
            }
        });
    }

    @Override
    public void updateLight() {
        relight(pos, tunnelFlaps.values().stream().flatMap(Collection::stream));
    }

    @Override
    public void remove() {
        tunnelFlaps.values()
                   .stream()
                   .flatMap(Collection::stream)
                   .forEach(InstanceData::delete);
    }
}
