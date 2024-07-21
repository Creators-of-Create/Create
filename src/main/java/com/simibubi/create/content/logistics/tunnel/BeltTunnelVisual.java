package com.simibubi.create.content.logistics.tunnel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.flwdata.FlapInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

public class BeltTunnelVisual extends AbstractBlockEntityVisual<BeltTunnelBlockEntity> implements SimpleDynamicVisual {

    private final Map<Direction, ArrayList<FlapInstance>> tunnelFlaps = new EnumMap<>(Direction.class);

    public BeltTunnelVisual(VisualizationContext context, BeltTunnelBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

		setupFlaps(partialTick);
	}

	private void setupFlaps(float partialTick) {
		Instancer<FlapInstance> model = instancerProvider.instancer(AllInstanceTypes.FLAP, Models.partial(AllPartialModels.BELT_TUNNEL_FLAP));

		int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
		int skyLight = level.getBrightness(LightLayer.SKY, pos);

		blockEntity.flaps.forEach((direction, flapValue) -> {

			float flapness = flapValue.getValue(partialTick);

			float horizontalAngle = direction.getOpposite().toYRot();

			float flapScale = direction.getAxis() == Direction.Axis.X ? 1 : -1;

			ArrayList<FlapInstance> flaps = new ArrayList<>(4);

			for (int segment = 0; segment <= 3; segment++) {
				float intensity = segment == 3 ? 1.5f : segment + 1;
				float segmentOffset = -3.05f / 16f * segment + 0.075f / 16f;

				FlapInstance key = model.createInstance();

				key.setPosition(getVisualPosition())
						.setSegmentOffset(segmentOffset, 0, 0)
						.light(blockLight, skyLight)
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
	public void update(float partialTick) {
		super.update(partialTick);

		_delete();
		setupFlaps(partialTick);
	}

	@Override
    public void beginFrame(DynamicVisual.Context ctx) {
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
    public void updateLight(float partialTick) {
        relight(tunnelFlaps.values().stream().flatMap(Collection::stream).toArray(FlatLit[]::new));
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
