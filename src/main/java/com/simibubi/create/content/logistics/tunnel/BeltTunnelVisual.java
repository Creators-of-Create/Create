package com.simibubi.create.content.logistics.tunnel;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.FlapStuffs;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.minecraft.core.Direction;

public class BeltTunnelVisual extends AbstractBlockEntityVisual<BeltTunnelBlockEntity> implements SimpleDynamicVisual {

	private final Map<Direction, FlapStuffs.Visual> tunnelFlaps = new EnumMap<>(Direction.class);
	private int light;

	public BeltTunnelVisual(VisualizationContext context, BeltTunnelBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

		createFlaps();
		updateFlaps(partialTick);
	}

	private void createFlaps() {
		blockEntity.flaps.forEach((direction, flapValue) -> {
			var commonTransform = FlapStuffs.commonTransform(visualPos, direction, 0);
			var flapSide = new FlapStuffs.Visual(instancerProvider(), commonTransform, FlapStuffs.TUNNEL_PIVOT, Models.partial(AllPartialModels.BELT_TUNNEL_FLAP));

			flapSide.updateLight(light);

			tunnelFlaps.put(direction, flapSide);
		});
	}

	@Override
	public void update(float partialTick) {
		super.update(partialTick);

		_delete();
		createFlaps();
		updateFlaps(partialTick);
	}

	@Override
    public void beginFrame(DynamicVisual.Context ctx) {
		updateFlaps(ctx.partialTick());
	}

	private void updateFlaps(float partialTicks) {
		tunnelFlaps.forEach((direction, keys) -> {
			LerpedFloat lerpedFloat = blockEntity.flaps.get(direction);
			if (lerpedFloat == null) {
				return;
			}

			keys.update(lerpedFloat.getValue(partialTicks));
        });
	}

	@Override
    public void updateLight(float partialTick) {
		// Need to save the packed light in case we need to recreate the instances.
		light = computePackedLight();
		for (FlapStuffs.Visual value : tunnelFlaps.values()) {
			value.updateLight(light);
		}
    }

    @Override
    protected void _delete() {
        tunnelFlaps.values()
                   .forEach(FlapStuffs.Visual::delete);

		tunnelFlaps.clear();
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		for (FlapStuffs.Visual value : tunnelFlaps.values()) {
			value.collectCrumblingInstances(consumer);
		}
	}
}
