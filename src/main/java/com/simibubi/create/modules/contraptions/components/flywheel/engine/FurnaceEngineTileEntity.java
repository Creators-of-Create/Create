package com.simibubi.create.modules.contraptions.components.flywheel.engine;

import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class FurnaceEngineTileEntity extends EngineTileEntity {

	public FurnaceEngineTileEntity() {
		super(AllTileEntities.FURNACE_ENGINE.type);
	}

	@Override
	public void lazyTick() {
		updateFurnace();
		super.lazyTick();
	}

	public void updateFurnace() {
		BlockState state = world.getBlockState(EngineBlock.getBaseBlockPos(getBlockState(), pos));
		if (!(state.getBlock() instanceof AbstractFurnaceBlock))
			return;

		float modifier = state.getBlock() == Blocks.BLAST_FURNACE ? 2 : 1;
		boolean active = state.has(AbstractFurnaceBlock.LIT) && state.get(AbstractFurnaceBlock.LIT);
		float speed = active ? 16 * modifier : 0;
		float capacity =
			(float) (active ? AllConfigs.SERVER.kinetics.stressValues.getCapacityOf(AllBlocksNew.FURNACE_ENGINE.get())
				: 0);

		appliedCapacity = capacity;
		appliedSpeed = speed;
		refreshWheelSpeed();
	}

}
