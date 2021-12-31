package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineModifiers.EngineState;
import com.simibubi.create.foundation.block.BlockStressValues;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceEngineTileEntity extends EngineTileEntity {

	public FurnaceEngineTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void lazyTick() {
		updateFurnace();
		super.lazyTick();
	}

	public void updateFurnace() {
		BlockState state = level.getBlockState(EngineBlock.getBaseBlockPos(getBlockState(), worldPosition));
		EngineState engineState = FurnaceEngineModifiers.get().getEngineState(state);
		if (engineState.isEmpty())
			return;

		float modifier = FurnaceEngineModifiers.get().getModifier(state);
		boolean active = engineState.isActive();
		float speed = active ? 16 * modifier : 0;
		float capacity =
			(float) (active ? BlockStressValues.getCapacity(AllBlocks.FURNACE_ENGINE.get())
				: 0);

		appliedCapacity = capacity;
		appliedSpeed = speed;
		refreshWheelSpeed();
	}

}
