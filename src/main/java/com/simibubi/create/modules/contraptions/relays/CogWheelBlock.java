package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class CogWheelBlock extends ShaftBlock {

	private boolean isLarge;

	protected static final VoxelShape GEAR_X = makeCuboidShape(6, 2, 2, 10, 14, 14);
	protected static final VoxelShape GEAR_Y = makeCuboidShape(2, 6, 2, 14, 10, 14);
	protected static final VoxelShape GEAR_Z = makeCuboidShape(2, 2, 6, 14, 14, 10);

	protected static final VoxelShape LARGE_GEAR_X = makeCuboidShape(6, 0, 0, 10, 16, 16);
	protected static final VoxelShape LARGE_GEAR_Y = makeCuboidShape(0, 6, 0, 16, 10, 16);
	protected static final VoxelShape LARGE_GEAR_Z = makeCuboidShape(0, 0, 6, 16, 16, 10);

	public CogWheelBlock(boolean large) {
		super(Properties.from(Blocks.STRIPPED_SPRUCE_LOG));
		isLarge = large;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.or(super.getShape(state, worldIn, pos, context), getGearShape(state));
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		for (Direction facing : Direction.values()) {
			if (facing.getAxis() == state.get(AXIS))
				continue;

			BlockState blockState = worldIn.getBlockState(pos.offset(facing));
			if (AllBlocks.LARGE_COGWHEEL.typeOf(blockState) || isLarge && AllBlocks.COGWHEEL.typeOf(blockState))
				return false;
		}
		return true;
	}

	private VoxelShape getGearShape(BlockState state) {
		if (state.get(AXIS) == Axis.X)
			return isLarge ? LARGE_GEAR_X : GEAR_X;
		if (state.get(AXIS) == Axis.Z)
			return isLarge ? LARGE_GEAR_Z : GEAR_Z;
		
		return isLarge ? LARGE_GEAR_Y : GEAR_Y;
	}
	
	// IRotate
	
	@Override
	public boolean hasCogsTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return !isLarge && face.getAxis() != state.get(AXIS);
	}

}
