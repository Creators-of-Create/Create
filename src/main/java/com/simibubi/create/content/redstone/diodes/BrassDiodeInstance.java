package com.simibubi.create.content.redstone.diodes;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.TickableVisual;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.Color;

public class BrassDiodeInstance extends BlockEntityInstance<BrassDiodeBlockEntity> implements TickableVisual {

    protected final ModelData indicator;

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
    public void tick() {
        if (previousState == blockEntity.state) return;

        indicator.setColor(getColor());

        previousState = blockEntity.state;
    }

    @Override
    public void updateLight() {
        relight(pos, indicator);
    }

    @Override
    public void remove() {
        indicator.delete();
    }

    protected int getColor() {
        return Color.mixColors(0x2c0300, 0xcd0000, blockEntity.getProgress());
    }
}
