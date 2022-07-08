package com.simibubi.create.content.logistics.block.diodes;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.Color;

public class BrassDiodeInstance extends BlockEntityInstance<BrassDiodeTileEntity> implements TickableInstance {

    protected final ModelData indicator;

    protected int previousState;

    public BrassDiodeInstance(MaterialManager modelManager, BrassDiodeTileEntity tile) {
        super(modelManager, tile);

        indicator = modelManager.defaultSolid()
                .material(Materials.TRANSFORMED)
                .getModel(AllBlockPartials.FLEXPEATER_INDICATOR, blockState).createInstance();

        indicator.loadIdentity()
				.translate(getInstancePosition())
				.setColor(getColor());

        previousState = tile.state;
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
