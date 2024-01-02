package com.simibubi.create.content.kinetics.drill;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DrillInstance extends SingleRotatingInstance<DrillBlockEntity> {

    public DrillInstance(VisualizationContext materialManager, DrillBlockEntity blockEntity) {
        super(materialManager, blockEntity);
    }

	@Override
	protected Model model() {
        Direction facing = blockEntity.getBlockState()
                .getValue(BlockStateProperties.FACING);
		return Models.partial(AllPartialModels.DRILL_HEAD, facing);
	}
}
