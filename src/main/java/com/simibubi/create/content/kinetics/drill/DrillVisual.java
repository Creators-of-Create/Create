package com.simibubi.create.content.kinetics.drill;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DrillVisual extends SingleRotatingVisual<DrillBlockEntity> {

    public DrillVisual(VisualizationContext context, DrillBlockEntity blockEntity) {
        super(context, blockEntity);
    }

	@Override
	protected Model model() {
        Direction facing = blockEntity.getBlockState()
                .getValue(BlockStateProperties.FACING);
		return Models.partial(AllPartialModels.DRILL_HEAD, facing);
	}
}
