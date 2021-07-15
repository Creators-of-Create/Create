package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class CrateBlock extends ProperDirectionalBlock implements IWrenchable {

	public static final BooleanProperty DOUBLE = BooleanProperty.create("double");

	public CrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP)
			.setValue(DOUBLE, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CRATE_BLOCK_SHAPE;
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {

		boolean isDouble = stateIn.getValue(DOUBLE);
		Direction blockFacing = stateIn.getValue(FACING);
		boolean isFacingOther = facingState.getBlock() == this && facingState.getValue(DOUBLE)
			&& facingState.getValue(FACING) == facing.getOpposite();

		if (!isDouble) {
			if (!isFacingOther)
				return stateIn;
			return stateIn.setValue(DOUBLE, true)
				.setValue(FACING, facing);
		}

		if (facing != blockFacing)
			return stateIn;
		if (!isFacingOther)
			return stateIn.setValue(DOUBLE, false);

		return stateIn;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos pos = context.getClickedPos();
		World world = context.getLevel();

		if (context.getPlayer() == null || !context.getPlayer()
			.isShiftKeyDown()) {
			for (Direction d : Iterate.directions) {
				BlockState state = world.getBlockState(pos.relative(d));
				if (state.getBlock() == this && !state.getValue(DOUBLE))
					return defaultBlockState().setValue(FACING, d)
						.setValue(DOUBLE, true);
			}
		}

		Direction placedOnFace = context.getClickedFace()
			.getOpposite();
		BlockState state = world.getBlockState(pos.relative(placedOnFace));
		if (state.getBlock() == this && !state.getValue(DOUBLE))
			return defaultBlockState().setValue(FACING, placedOnFace)
				.setValue(DOUBLE, true);
		return defaultBlockState();
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		return originalState;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(DOUBLE));
	}

}
