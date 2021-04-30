package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class DrillInstance extends SingleRotatingInstance {

    public DrillInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
		BlockState referenceState = tile.getBlockState();
		Direction facing = referenceState.get(FACING);
		return getRotatingMaterial().getModel(AllBlockPartials.DRILL_HEAD, referenceState, facing);
	}
}
