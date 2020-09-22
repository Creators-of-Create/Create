package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.config.AllConfigs;

import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntityType;

public class FurnaceEngineTileEntity extends EngineTileEntity {

	public FurnaceEngineTileEntity(TileEntityType<? extends FurnaceEngineTileEntity> type) {
		super(type);
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
		boolean active = BlockHelper.hasBlockStateProperty(state, AbstractFurnaceBlock.LIT) && state.get(AbstractFurnaceBlock.LIT);
		float speed = active ? 16 * modifier : 0;
		float capacity =
			(float) (active ? AllConfigs.SERVER.kinetics.stressValues.getCapacityOf(AllBlocks.FURNACE_ENGINE.get())
				: 0);

		appliedCapacity = capacity;
		appliedSpeed = speed;
		refreshWheelSpeed();
	}

}
