package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CrateBlock extends ProperDirectionalBlock implements IWrenchable {

	public static final BooleanProperty DOUBLE = BooleanProperty.create("double");

	public CrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		setDefaultState(getDefaultState().with(FACING, Direction.UP)
			.with(DOUBLE, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CRATE_BLOCK_SHAPE;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {

		boolean isDouble = stateIn.get(DOUBLE);
		Direction blockFacing = stateIn.get(FACING);
		boolean isFacingOther = facingState.getBlock() == this && facingState.get(DOUBLE)
			&& facingState.get(FACING) == facing.getOpposite();

		if (!isDouble) {
			if (!isFacingOther)
				return stateIn;
			return stateIn.with(DOUBLE, true)
				.with(FACING, facing);
		}

		if (facing != blockFacing)
			return stateIn;
		if (!isFacingOther)
			return stateIn.with(DOUBLE, false);

		return stateIn;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos pos = context.getPos();
		World world = context.getWorld();

		if (context.getPlayer() == null || !context.getPlayer()
			.isSneaking()) {
			for (Direction d : Iterate.directions) {
				BlockState state = world.getBlockState(pos.offset(d));
				if (state.getBlock() == this && !state.get(DOUBLE))
					return getDefaultState().with(FACING, d)
						.with(DOUBLE, true);
			}
		}

		Direction placedOnFace = context.getFace()
			.getOpposite();
		BlockState state = world.getBlockState(pos.offset(placedOnFace));
		if (state.getBlock() == this && !state.get(DOUBLE))
			return getDefaultState().with(FACING, placedOnFace)
				.with(DOUBLE, true);
		return getDefaultState();
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		return originalState;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(DOUBLE));
	}

}
