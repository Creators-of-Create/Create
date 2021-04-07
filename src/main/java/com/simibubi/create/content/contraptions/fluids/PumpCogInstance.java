package com.simibubi.create.content.contraptions.fluids;

import net.minecraft.block.BlockState;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class PumpCogInstance extends SingleRotatingInstance {

    public PumpCogInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        BlockState referenceState = tile.getBlockState();
        return AllBlockPartials.MECHANICAL_PUMP_COG.getModel(getRotatingMaterial(), referenceState, referenceState.get(FACING));
    }
}
