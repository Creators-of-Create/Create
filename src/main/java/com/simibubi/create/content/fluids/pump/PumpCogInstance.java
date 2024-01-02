package com.simibubi.create.content.fluids.pump;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PumpCogInstance extends SingleRotatingInstance<PumpBlockEntity> implements DynamicVisual {

	public PumpCogInstance(VisualizationContext materialManager, PumpBlockEntity blockEntity) {
		super(materialManager, blockEntity);
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
