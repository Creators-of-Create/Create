package com.simibubi.create.content.logistics.tunnel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.flwdata.FlapInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

public class BeltTunnelVisual extends AbstractBlockEntityVisual<BeltTunnelBlockEntity> implements SimpleDynamicVisual {

    private final Map<Direction, ArrayList<FlapInstance>> tunnelFlaps;

    public BeltTunnelVisual(VisualizationContext context, BeltTunnelBlockEntity blockEntity) {
        super(context, blockEntity);

        tunnelFlaps = new EnumMap<>(Direction.class);

        Instancer<FlapInstance> model = instancerProvider.instancer(AllInstanceTypes.FLAPS, Models.partial(AllPartialModels.BELT_TUNNEL_FLAP));

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);

        blockEntity.flaps.forEach((direction, flapValue) -> {

            float flapness = flapValue.getValue(AnimationTickHolder.getPartialTicks());

            float horizontalAngle = direction.getOpposite().toYRot();

            float flapScale = direction.getAxis() == Direction.Axis.X ? 1 : -1;

            ArrayList<FlapInstance> flaps = new ArrayList<>(4);

            for (int segment = 0; segment <= 3; segment++) {
                float intensity = segment == 3 ? 1.5f : segment + 1;
                float segmentOffset = -3.05f / 16f * segment + 0.075f / 16f;

                FlapInstance key = model.createInstance();

                key.setPosition(getVisualPosition())
						.setSegmentOffset(segmentOffset, 0, 0)
						.setBlockLight(blockLight)
						.setSkyLight(skyLight)
						.setHorizontalAngle(horizontalAngle)
						.setFlapness(flapness)
						.setFlapScale(flapScale)
						.setPivotVoxelSpace(0, 10, 1)
						.setIntensity(intensity)
						.setChanged();

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
    public void beginFrame(VisualFrameContext ctx) {
        tunnelFlaps.forEach((direction, keys) -> {
            LerpedFloat lerpedFloat = blockEntity.flaps.get(direction);
            if (lerpedFloat == null)
                return;

            float flapness = lerpedFloat.getValue(ctx.partialTick());
            for (FlapInstance flap : keys) {
                flap.setFlapness(flapness)
						.setChanged();
            }
        });
    }

    @Override
    public void updateLight() {
        relight(pos, tunnelFlaps.values().stream().flatMap(Collection::stream));
    }

    @Override
    protected void _delete() {
        tunnelFlaps.values()
                   .stream()
                   .flatMap(Collection::stream)
                   .forEach(AbstractInstance::delete);
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		tunnelFlaps.values()
				   .stream()
				   .flatMap(Collection::stream)
				   .forEach(consumer);
	}
}
