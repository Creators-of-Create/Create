package com.simibubi.create.content.kinetics.saw;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawInstance extends SingleRotatingInstance<SawBlockEntity> {

	public SawInstance(VisualizationContext materialManager, SawBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Model model() {
		if (blockState.getValue(BlockStateProperties.FACING)
				.getAxis()
				.isHorizontal()) {
			BlockState referenceState = blockState.rotate(blockEntity.getLevel(), blockEntity.getBlockPos(), Rotation.CLOCKWISE_180);
			Direction facing = referenceState.getValue(BlockStateProperties.FACING);
			return Models.partial(AllPartialModels.SHAFT_HALF, facing);
		} else {
			return VirtualRenderHelper.blockModel(shaft());
		}
	}
}
