package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HalfShaftVisual<T extends KineticBlockEntity> extends SingleRotatingVisual<T> {
    public HalfShaftVisual(VisualizationContext context, T blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

	@Override
	protected Model model() {
		return Models.partial(AllPartialModels.SHAFT_HALF, getShaftDirection());
	}

	protected Direction getShaftDirection() {
        return blockState.getValue(BlockStateProperties.FACING);
    }
}
