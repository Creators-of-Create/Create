package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

public class ShaftlessCogInstance extends SingleRotatingInstance {

    public ShaftlessCogInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
		return renderer.getMaterial(AllMaterialSpecs.ROTATING).getModel(AllBlockPartials.SHAFTLESS_COGWHEEL, tile.getBlockState());
	}
}
