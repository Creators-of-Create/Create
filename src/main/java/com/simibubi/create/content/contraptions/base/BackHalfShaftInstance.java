package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.instancing.MaterialManager;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class BackHalfShaftInstance extends HalfShaftInstance {
    public BackHalfShaftInstance(MaterialManager<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Direction getShaftDirection() {
        return tile.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
    }
}
