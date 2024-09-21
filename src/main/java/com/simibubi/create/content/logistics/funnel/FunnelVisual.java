package com.simibubi.create.content.logistics.funnel;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.flwdata.FlapInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

public class FunnelVisual extends AbstractBlockEntityVisual<FunnelBlockEntity> implements SimpleDynamicVisual {

	private final ArrayList<FlapInstance> flaps;

    public FunnelVisual(VisualizationContext context, FunnelBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        flaps = new ArrayList<>(4);

        if (!blockEntity.hasFlap()) return;

		PartialModel flapPartial = (blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP
				: AllPartialModels.BELT_FUNNEL_FLAP);
        Instancer<FlapInstance> model = instancerProvider().instancer(AllInstanceTypes.FLAP, Models.partial(flapPartial));

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);

        Direction direction = FunnelBlock.getFunnelFacing(blockState);

        float flapness = blockEntity.flap.getValue(AnimationTickHolder.getPartialTicks());
        float horizontalAngle = direction.getOpposite().toYRot();

        for (int segment = 0; segment <= 3; segment++) {
            float intensity = segment == 3 ? 1.5f : segment + 1;
            float segmentOffset = -3.05f / 16f * segment + 0.075f / 16f;

            FlapInstance key = model.createInstance();

            key.setPosition(getVisualPosition())
					.setSegmentOffset(segmentOffset, 0, -blockEntity.getFlapOffset())
					.light(blockLight, skyLight)
					.setHorizontalAngle(horizontalAngle)
					.setFlapness(flapness)
					.setFlapScale(-1)
					.setPivotVoxelSpace(0, 10, 9.5f)
					.setIntensity(intensity)
					.setChanged();

            flaps.add(key);
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (flaps == null) return;

        float flapness = blockEntity.flap.getValue(ctx.partialTick());

        for (FlapInstance flap : flaps) {
            flap.setFlapness(flapness)
					.setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        if (flaps != null)
            relight(flaps.toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        if (flaps == null) return;

        flaps.forEach(AbstractInstance::delete);
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		if (flaps == null) return;

		flaps.forEach(consumer);
	}
}
