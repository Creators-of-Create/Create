package com.simibubi.create.modules.logistics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ExtractorTileEntity();
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		return false;
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
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateObservedInventory(state, worldIn, pos);
	}
	
	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		if (world.isRemote())
			return;
		if (!isObserving(state, pos, neighbor))
			return;
		updateObservedInventory(state, world, pos);
	}
	
	private void updateObservedInventory(BlockState state, IWorldReader world, BlockPos pos) {
		IExtractor extractor = (IExtractor) world.getTileEntity(pos);
		if (extractor == null)
			return;
		extractor.neighborChanged();
	}
	
	private boolean isObserving(BlockState state, BlockPos pos, BlockPos observing) {
		return observing.equals(pos.offset(state.get(HORIZONTAL_FACING)));
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
			IExtractor extractor = (IExtractor) worldIn.getTileEntity(pos);
			if (extractor == null)
				return;
			extractor.setLocked(!previouslyPowered);
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
