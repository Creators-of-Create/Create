package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class RealityFunnelBlock extends ProperDirectionalBlock {

	public RealityFunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.REALITY_FUNNEL.get(state.get(FACING));
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = state.get(FACING)
			.getOpposite();
		if (fromPos.equals(pos.offset(blockFacing)))
			if (!isValidPosition(state, worldIn, pos))
				worldIn.destroyBlock(pos, true);
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		return !world.getBlockState(pos.offset(state.get(FACING)
			.getOpposite()))
			.getShape(world, pos)
			.isEmpty();
	}
	

}
