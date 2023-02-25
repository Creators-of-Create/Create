package com.simibubi.create.content.contraptions.components.saw;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawInstance extends SingleRotatingInstance<SawBlockEntity> {

	public SawInstance(MaterialManager materialManager, SawBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		if (blockState.getValue(BlockStateProperties.FACING)
			.getAxis()
			.isHorizontal()) {
			BlockState referenceState = blockState.rotate(blockEntity.getLevel(), blockEntity.getBlockPos(), Rotation.CLOCKWISE_180);
			Direction facing = referenceState.getValue(BlockStateProperties.FACING);
			return getRotatingMaterial().getModel(AllPartialModels.SHAFT_HALF, referenceState, facing);
		} else {
			return getRotatingMaterial().getModel(shaft());
		}
	}
}
