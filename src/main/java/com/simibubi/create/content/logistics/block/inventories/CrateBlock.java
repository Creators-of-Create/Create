package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CrateBlock extends WrenchableDirectionalBlock implements IWrenchable {

	public static final BooleanProperty DOUBLE = BooleanProperty.create("double");

	public CrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP)
			.setValue(DOUBLE, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.CRATE_BLOCK_SHAPE;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level world = context.getLevel();

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
