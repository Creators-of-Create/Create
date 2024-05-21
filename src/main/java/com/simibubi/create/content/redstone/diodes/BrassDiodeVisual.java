package com.simibubi.create.content.redstone.diodes;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.jozufozu.flywheel.lib.visual.SimpleTickableVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.Color;

public class BrassDiodeVisual extends AbstractBlockEntityVisual<BrassDiodeBlockEntity> implements SimpleTickableVisual {

    protected final TransformedInstance indicator;

    protected int previousState;

    public BrassDiodeVisual(VisualizationContext context, BrassDiodeBlockEntity blockEntity) {
        super(context, blockEntity);

        indicator = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FLEXPEATER_INDICATOR)).createInstance();

        indicator.loadIdentity()
				.translate(getVisualPosition())
				.setColor(getColor())
				.setChanged();

        previousState = blockEntity.state;
    }

    @Override
    public void tick(TickableVisual.Context context) {
        if (previousState == blockEntity.state) return;

        indicator.setColor(getColor());
		indicator.setChanged();

        previousState = blockEntity.state;
    }

    @Override
    public void updateLight() {
        relight(pos, indicator);
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
