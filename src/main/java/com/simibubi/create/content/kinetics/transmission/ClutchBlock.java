package com.simibubi.create.content.kinetics.transmission;

import com.simibubi.create.AllBlockEntityTypes;

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
			detachKinetics(worldIn, pos, previouslyPowered);
		}
	}
	
	@Override
	public BlockEntityType<? extends SplitShaftBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CLUTCH.get();
	}

}
