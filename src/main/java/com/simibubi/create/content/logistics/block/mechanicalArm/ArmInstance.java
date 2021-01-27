package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.RotatingData;

public class ArmInstance extends SingleRotatingInstance {
    public ArmInstance(InstancedTileRenderDispatcher modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        return AllBlockPartials.ARM_COG.renderOnRotating(modelManager, tile.getBlockState());
    }
}
