package com.simibubi.create.content.contraptions.fluids;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class PumpCogInstance extends SingleRotatingInstance {

    public PumpCogInstance(MaterialManager<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
		BlockState referenceState = tile.getBlockState();
		Direction facing = referenceState.getValue(FACING);
		return getRotatingMaterial().getModel(AllBlockPartials.MECHANICAL_PUMP_COG, referenceState, facing);
	}
}
