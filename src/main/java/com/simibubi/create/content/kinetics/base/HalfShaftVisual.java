package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HalfShaftVisual<T extends KineticBlockEntity> extends SingleRotatingVisual<T> {
    public HalfShaftVisual(VisualizationContext context, T blockEntity) {
        super(context, blockEntity);
    }

	@Override
	protected Model model() {
		return Models.partial(AllPartialModels.SHAFT_HALF, getShaftDirection());
	}

	protected Direction getShaftDirection() {
        return blockState.getValue(BlockStateProperties.FACING);
    }
}
