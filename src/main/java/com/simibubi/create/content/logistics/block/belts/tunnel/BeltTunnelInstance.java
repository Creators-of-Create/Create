package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.logistics.block.flap.FlapData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.InterpolatedValue;

import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

public class BeltTunnelInstance extends BlockEntityInstance<BeltTunnelTileEntity> implements DynamicInstance {

    private final Map<Direction, ArrayList<FlapData>> tunnelFlaps;

    public BeltTunnelInstance(MaterialManager modelManager, BeltTunnelTileEntity tile) {
        super(modelManager, tile);

        tunnelFlaps = new EnumMap<>(Direction.class);

        Instancer<FlapData> model = modelManager.defaultSolid()
                .material(AllMaterialSpecs.FLAPS)
				.getModel(AllBlockPartials.BELT_TUNNEL_FLAP, blockState);

        int blockLight = world.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = world.getBrightness(LightLayer.SKY, pos);

        tile.flaps.forEach((direction, flapValue) -> {

            float flapness = flapValue.get(AnimationTickHolder.getPartialTicks());

            float horizontalAngle = direction.getOpposite().toYRot();

            float flapScale = direction.getAxis() == Direction.Axis.X ? 1 : -1;

            ArrayList<FlapData> flaps = new ArrayList<>(4);

            for (int segment = 0; segment <= 3; segment++) {
                float intensity = segment == 3 ? 1.5f : segment + 1;
                float segmentOffset = -3 / 16f * segment;

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
            InterpolatedValue flapValue = blockEntity.flaps.get(direction);
            if (flapValue == null) {
                return;
            }

            float flapness = flapValue.get(AnimationTickHolder.getPartialTicks());
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
