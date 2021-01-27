package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public class BackHalfShaftInstance extends HalfShaftInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        InstancedTileRenderRegistry.instance.register(type, BackHalfShaftInstance::new);
    }

    public BackHalfShaftInstance(InstancedTileRenderDispatcher modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Direction getShaftDirection() {
        return tile.getBlockState().get(BlockStateProperties.FACING).getOpposite();
    }
}
