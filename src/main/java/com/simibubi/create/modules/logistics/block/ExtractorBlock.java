package com.simibubi.create.modules.logistics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ExtractorBlock extends HorizontalBlock {

	public static BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final VoxelShape SHAPE_NORTH = makeCuboidShape(4, 2, -1, 12, 10, 5),
			SHAPE_SOUTH = makeCuboidShape(4, 2, 11, 12, 10, 17), SHAPE_WEST = makeCuboidShape(-1, 2, 4, 5, 10, 12),
			SHAPE_EAST = makeCuboidShape(11, 2, 4, 17, 10, 12);

	public ExtractorBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, POWERED);
		super.fillStateContainer(builder);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace().getOpposite());
		} else {
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
		}

		return state.with(POWERED, Boolean.valueOf(context.getWorld().isBlockPowered(context.getPos())));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(HORIZONTAL_FACING);

		if (facing == Direction.EAST)
			return SHAPE_EAST;
		if (facing == Direction.WEST)
			return SHAPE_WEST;
		if (facing == Direction.SOUTH)
			return SHAPE_SOUTH;
		if (facing == Direction.NORTH)
			return SHAPE_NORTH;

		return VoxelShapes.empty();
	}

}
