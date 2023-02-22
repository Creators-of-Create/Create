package com.simibubi.create.content.contraptions.components.millstone;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

public class MillstoneCogInstance extends SingleRotatingInstance<MillstoneBlockEntity> {

    public MillstoneCogInstance(MaterialManager materialManager, MillstoneBlockEntity blockEntity) {
        super(materialManager, blockEntity);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        return getRotatingMaterial().getModel(AllBlockPartials.MILLSTONE_COG, blockEntity.getBlockState());
    }
}
