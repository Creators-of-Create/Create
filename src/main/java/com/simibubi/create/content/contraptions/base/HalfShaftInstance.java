package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class HalfShaftInstance extends SingleRotatingInstance {
    public HalfShaftInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        Direction dir = getShaftDirection();
        return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(renderer, blockState, dir);
    }

    protected Direction getShaftDirection() {
        return blockState.get(BlockStateProperties.FACING);
    }
}
