package com.simibubi.create.content.logistics.block.diodes;

import com.jozufozu.flywheel.backend.instancing.ITickableInstance;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.TileEntityInstance;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

public class AdjustableRepeaterInstance extends TileEntityInstance<AdjustableRepeaterTileEntity> implements ITickableInstance {

    protected final ModelData indicator;

    protected int previousState;

    public AdjustableRepeaterInstance(InstancedTileRenderer<?> modelManager, AdjustableRepeaterTileEntity tile) {
        super(modelManager, tile);

        indicator = modelManager.getTransformMaterial().getModel(AllBlockPartials.FLEXPEATER_INDICATOR, blockState).createInstance();

        MatrixStack ms = new MatrixStack();
        MatrixStacker.of(ms).translate(getInstancePosition());

        indicator
                 .setTransform(ms)
                 .setColor(getColor());

        previousState = tile.state;
    }

    @Override
    public void tick() {
        if (previousState == tile.state) return;

        indicator.setColor(getColor());

        previousState = tile.state;
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
        return ColorHelper.mixColors(0x2C0300, 0xCD0000, tile.state / (float) tile.maxState.getValue());
    }
}
