package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.foundation.block.IWithTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalBearingBlock extends BearingBlock implements IWithTileEntity<MechanicalBearingTileEntity> {

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalBearingTileEntity();
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.isAllowEdit())
			return false;
		if (player.isSneaking())
			return false;
		if (player.getHeldItem(handIn).isEmpty()) {
			if (!worldIn.isRemote) {
				withTileEntityDo(worldIn, pos, te -> {
					if (te.running) {
						te.disassemble();
						return;
					}
					te.assembleNextTick = true;
				});
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		withTileEntityDo(worldIn, pos, MechanicalBearingTileEntity::neighbourChanged);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		withTileEntityDo(worldIn, pos, MechanicalBearingTileEntity::neighbourChanged);
	}

}
