package com.simibubi.create.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

public class SchematicannonBlock extends Block {

	public SchematicannonBlock() {
		super(Properties.from(Blocks.DISPENSER));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SchematicannonTileEntity();
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		((SchematicannonTileEntity) worldIn.getTileEntity(currentPos)).findInventories();
		return stateIn;
	}
	
	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		((SchematicannonTileEntity) world.getTileEntity(pos)).findInventories();
		super.onNeighborChange(state, world, pos, neighbor);
	}

}
