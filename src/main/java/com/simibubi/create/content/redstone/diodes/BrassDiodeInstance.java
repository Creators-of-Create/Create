package com.simibubi.create.content.redstone.diodes;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.Color;

public class BrassDiodeInstance extends AbstractBlockEntityVisual<BrassDiodeBlockEntity> implements TickableVisual {

    protected final TransformedInstance indicator;

    protected int previousState;

    public BrassDiodeInstance(VisualizationContext materialManager, BrassDiodeBlockEntity blockEntity) {
        super(materialManager, blockEntity);

        indicator = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FLEXPEATER_INDICATOR), RenderStage.AFTER_BLOCK_ENTITIES).createInstance();

        indicator.loadIdentity()
				.translate(getVisualPosition())
				.setColor(getColor());

        previousState = blockEntity.state;
    }

    @Override
    public void tick(VisualTickContext ctx) {
        if (previousState == blockEntity.state) return;

        indicator.setColor(getColor());

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
