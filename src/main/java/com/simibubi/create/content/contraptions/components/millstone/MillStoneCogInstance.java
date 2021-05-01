package com.simibubi.create.content.contraptions.components.millstone;

import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

public class MillStoneCogInstance extends SingleRotatingInstance {

    public MillStoneCogInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        return getRotatingMaterial().getModel(AllBlockPartials.MILLSTONE_COG, tile.getBlockState());
    }
}
