package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class PumpBlock extends DirectionalKineticBlock {

	public PumpBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.MECHANICAL_PUMP.create();
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		return originalState.with(FACING, originalState.get(FACING)
			.getOpposite());
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING)
			.getAxis();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.PUMP.get(state.get(FACING));
	}

	@Override
	public boolean hasIntegratedCogwheel(IWorldReader world, BlockPos pos, BlockState state) {
		return true;
	}

}
