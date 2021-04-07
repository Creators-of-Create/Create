package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

public class ShaftlessCogInstance extends SingleRotatingInstance {

    public ShaftlessCogInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
		return renderer.getMaterial(KineticRenderMaterials.ROTATING).getModel(AllBlockPartials.SHAFTLESS_COGWHEEL, tile.getBlockState());
	}
}
