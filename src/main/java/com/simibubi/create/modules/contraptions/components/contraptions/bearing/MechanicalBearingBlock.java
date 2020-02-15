package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.foundation.block.IWithTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalBearingBlock extends BearingBlock implements IWithTileEntity<MechanicalBearingTileEntity> {

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalBearingTileEntity();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		withTileEntityDo(worldIn, pos, MechanicalBearingTileEntity::neighbourChanged);
	}

}
