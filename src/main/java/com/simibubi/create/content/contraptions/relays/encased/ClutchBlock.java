package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllTileEntities;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ClutchBlock extends GearshiftBlock {

	public ClutchBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos)) {
			worldIn.setBlock(pos, state.cycle(POWERED), 2 | 16);
		}
	}

	@Override
	public BlockEntityType<? extends KineticTileEntity> getTileEntityType() {
		return AllTileEntities.CLUTCH.get();
	}

}
