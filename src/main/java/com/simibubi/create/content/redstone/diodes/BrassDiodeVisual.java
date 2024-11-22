package com.simibubi.create.content.redstone.diodes;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.Color;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;

public class BrassDiodeVisual extends AbstractBlockEntityVisual<BrassDiodeBlockEntity> implements SimpleTickableVisual {

    protected final TransformedInstance indicator;

    protected int previousState;

    public BrassDiodeVisual(VisualizationContext context, BrassDiodeBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        indicator = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FLEXPEATER_INDICATOR)).createInstance();

        indicator.setIdentityTransform()
				.translate(getVisualPosition())
				.colorRgb(getColor())
				.setChanged();

        previousState = blockEntity.state;
    }

    @Override
    public void tick(TickableVisual.Context context) {
        if (previousState == blockEntity.state) return;

        indicator.colorRgb(getColor());
		indicator.setChanged();

        previousState = blockEntity.state;
    }

    @Override
    public void updateLight(float partialTick) {
        relight(indicator);
    }

    @Override
    protected void _delete() {
        indicator.delete();
    }

    protected int getColor() {
        return Color.mixColors(0x2c0300, 0xcd0000, blockEntity.getProgress());
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(indicator);
	}
}
