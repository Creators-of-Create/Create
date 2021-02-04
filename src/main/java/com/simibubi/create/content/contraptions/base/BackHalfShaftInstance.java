package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class BackHalfShaftInstance extends HalfShaftInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, BackHalfShaftInstance::new));
    }

    public BackHalfShaftInstance(InstancedTileRenderer modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Direction getShaftDirection() {
        return tile.getBlockState().get(BlockStateProperties.FACING).getOpposite();
    }
}
