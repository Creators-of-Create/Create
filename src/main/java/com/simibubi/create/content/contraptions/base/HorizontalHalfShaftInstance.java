package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class HorizontalHalfShaftInstance extends HalfShaftInstance {

    public HorizontalHalfShaftInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Direction getShaftDirection() {
        return blockState.get(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
    }
}
