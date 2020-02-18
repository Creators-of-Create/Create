package com.simibubi.create.modules.contraptions.components.motor;

import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class MotorBlock extends HorizontalKineticBlock {

	public MotorBlock() {
		super(Properties.from(Blocks.IRON_BLOCK));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.MOTOR_BLOCK.get(state.get(HORIZONTAL_FACING));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MotorTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (context.isPlacerSneaking() || preferred == null)
			return super.getStateForPlacement(context);
		return getDefaultState().with(HORIZONTAL_FACING, preferred);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(HORIZONTAL_FACING);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}
}
