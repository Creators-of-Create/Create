package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class BackHalfShaftInstance extends HalfShaftInstance {
    public BackHalfShaftInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Direction getShaftDirection() {
        return tile.getBlockState().get(BlockStateProperties.FACING).getOpposite();
    }
}
