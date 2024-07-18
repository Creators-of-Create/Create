package com.simibubi.create.content.kinetics.base;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BackHalfShaftVisual<T extends KineticBlockEntity> extends HalfShaftVisual<T> {
    public BackHalfShaftVisual(VisualizationContext context, T blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @Override
    protected Direction getShaftDirection() {
        return blockState.getValue(BlockStateProperties.FACING).getOpposite();
    }
}
