package com.simibubi.create.modules.contraptions.relays.encased;

import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ClutchBlock extends GearshiftBlock {

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ClutchTileEntity();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2 | 16);
			TileEntity te = worldIn.getTileEntity(pos);
			if (te == null || !(te instanceof KineticTileEntity))
				return;
			if (previouslyPowered)
				RotationPropagator.handleAdded(worldIn, pos, (KineticTileEntity) te);
			else
				RotationPropagator.handleRemoved(worldIn, pos, (KineticTileEntity) te);
		}
	}

}
