package com.simibubi.create.content.contraptions.components.millstone;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

public class MillStoneCogInstance extends SingleRotatingInstance {

    public MillStoneCogInstance(MaterialManager modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        return getRotatingMaterial().getModel(AllBlockPartials.MILLSTONE_COG, blockEntity.getBlockState());
    }
}
