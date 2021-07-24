package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

public class ShaftlessCogInstance extends SingleRotatingInstance {

    public ShaftlessCogInstance(MaterialManager<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        return materialManager.defaultSolid()
                .material(AllMaterialSpecs.ROTATING)
                .getModel(AllBlockPartials.SHAFTLESS_COGWHEEL, tile.getBlockState());
	}
}
