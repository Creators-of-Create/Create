package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.material.MaterialManager;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;

public class BackHalfShaftInstance extends HalfShaftInstance {
    public BackHalfShaftInstance(MaterialManager modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Direction getShaftDirection() {
        return tile.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
    }
}
