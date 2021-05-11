package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlock;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

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
		BlockPos furnacePos = EngineBlock.getBaseBlockPos(getBlockState(), pos);
		BlockState state = world.getBlockState(furnacePos);
		if (!(state.getBlock() instanceof AbstractFurnaceBlock))
			return;
		
		for (Direction d : Direction.values()) {
			System.out.println(world.getBlockState(furnacePos.offset(d)));
			if (AllTags.AllBlockTags.FLYWHEELBLACKLIST.matches(world.getBlockState(furnacePos.offset(d)))) {
				world.destroyBlock(furnacePos.offset(d), true);
				return;
			}
		}

		float modifier = state.getBlock() == Blocks.BLAST_FURNACE ? 2 : 1;
		boolean active = state.contains(AbstractFurnaceBlock.LIT) && state.get(AbstractFurnaceBlock.LIT);
		float speed = active ? 16 * modifier : 0;
		float capacity =
			(float) (active ? AllConfigs.SERVER.kinetics.stressValues.getCapacityOf(AllBlocks.FURNACE_ENGINE.get())
				: 0);

		appliedCapacity = capacity;
		appliedSpeed = speed;
		refreshWheelSpeed();
	}

}
