package com.simibubi.create.content.fluids.pump;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PumpCogVisual extends SingleRotatingVisual<PumpBlockEntity> implements DynamicVisual {

	public PumpCogVisual(VisualizationContext context, PumpBlockEntity blockEntity) {
		super(context, blockEntity);
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {}

	@Override
	protected Model model() {
		BlockState referenceState = blockEntity.getBlockState();
		Direction facing = referenceState.getValue(BlockStateProperties.FACING);
		return Models.partial(AllPartialModels.MECHANICAL_PUMP_COG, facing);
	}
}
