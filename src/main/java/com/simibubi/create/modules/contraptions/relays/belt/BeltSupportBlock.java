package com.simibubi.create.modules.contraptions.relays.belt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BeltSupportBlock extends Block {

	public static final VoxelShape SHAPE = makeCuboidShape(0, 0, 0, 16, 27, 16);

	public BeltSupportBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return VoxelShapes.fullCube();
	}

	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return getRenderShape(state, worldIn, pos);
	}

}
