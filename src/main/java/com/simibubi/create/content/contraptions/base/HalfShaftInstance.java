package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public class HalfShaftInstance extends SingleRotatingInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        InstancedTileRenderRegistry.instance.register(type, HalfShaftInstance::new);
    }

    public HalfShaftInstance(InstancedTileRenderDispatcher modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        BlockState state = tile.getBlockState();
        Direction dir = getShaftDirection();
        return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, state, dir);
    }

    protected Direction getShaftDirection() {
        return tile.getBlockState().get(BlockStateProperties.FACING);
    }
}
