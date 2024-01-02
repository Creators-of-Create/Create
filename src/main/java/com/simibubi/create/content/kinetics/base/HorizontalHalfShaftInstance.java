package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HorizontalHalfShaftInstance<T extends KineticBlockEntity> extends HalfShaftInstance<T> {

    public HorizontalHalfShaftInstance(VisualizationContext materialManager, T blockEntity) {
        super(materialManager, blockEntity);
    }

    @Override
    protected Direction getShaftDirection() {
        return blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
    }
}
