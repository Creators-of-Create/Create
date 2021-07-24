package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.material.MaterialManager;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class HorizontalHalfShaftInstance extends HalfShaftInstance {

    public HorizontalHalfShaftInstance(MaterialManager<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Direction getShaftDirection() {
        return blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
    }
}
