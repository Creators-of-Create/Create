package com.simibubi.create.modules.logistics.block;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class RedstoneBridgeBlock extends ProperDirectionalBlock implements IBlockWithFrequency {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty RECEIVER = BooleanProperty.create("receiver");
	private static final List<Pair<Vec3d, Vec3d>> itemPositions = new ArrayList<>(Direction.values().length);

	public static final VoxelShape UP_SHAPE = makeCuboidShape(2, 0, 2, 14, 3, 14),
			DOWN_SHAPE = makeCuboidShape(2, 13, 2, 14, 16, 14);

	public static final VoxelShape SOUTH_SHAPE = makeCuboidShape(3, 1, -1, 13, 15, 3),
			NORTH_SHAPE = makeCuboidShape(3, 1, 13, 13, 15, 17), EAST_SHAPE = makeCuboidShape(-1, 1, 3, 3, 15, 13),
			WEST_SHAPE = makeCuboidShape(13, 1, 3, 17, 15, 13);

	public RedstoneBridgeBlock() {
		super(Properties.from(Blocks.DARK_OAK_LOG));
		cacheItemPositions();
		setDefaultState(getDefaultState().with(POWERED, false).with(RECEIVER, false));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		Direction blockFacing = state.get(FACING);

		if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}

		updateTransmittedSignal(state, worldIn, pos, blockFacing);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateTransmittedSignal(state, worldIn, pos, state.get(FACING));
	}

	private void updateTransmittedSignal(BlockState state, World worldIn, BlockPos pos, Direction blockFacing) {
		if (worldIn.isRemote)
			return;
		if (state.get(RECEIVER))
			return;

		boolean shouldPower = worldIn.getWorld().isBlockPowered(pos);
		boolean previouslyPowered = state.get(POWERED);

		if (previouslyPowered != shouldPower) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);

			RedstoneBridgeTileEntity te = (RedstoneBridgeTileEntity) worldIn.getTileEntity(pos);
			if (te == null)
				return;
			te.transmit(!previouslyPowered);
		}
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return state.get(POWERED) && state.get(RECEIVER);
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (side != blockState.get(FACING))
			return 0;
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (!state.get(RECEIVER))
			return 0;
		return state.get(POWERED) ? 15 : 0;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED, RECEIVER);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new RedstoneBridgeTileEntity();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {

		if (player.isSneaking()) {
			RedstoneBridgeTileEntity te = (RedstoneBridgeTileEntity) worldIn.getTileEntity(pos);
			if (te == null)
				return false;

			if (!worldIn.isRemote) {
				Boolean wasReceiver = state.get(RECEIVER);
				boolean blockPowered = worldIn.isBlockPowered(pos);
				worldIn.setBlockState(pos, state.cycle(RECEIVER).with(POWERED, blockPowered), 3);
				if (wasReceiver) {
					te.transmit(worldIn.isBlockPowered(pos));
				} else
					te.transmit(false);
			}
			return true;
		}

		return handleActivatedFrequencySlots(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return state.get(FACING) == Direction.UP && side != null;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.offset(state.get(FACING).getOpposite());
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return Block.hasSolidSide(neighbour, worldIn, neighbourPos, state.get(FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();
		state = state.with(FACING, context.getFace());
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(FACING);

		if (facing == Direction.UP)
			return UP_SHAPE;
		if (facing == Direction.DOWN)
			return DOWN_SHAPE;
		if (facing == Direction.EAST)
			return EAST_SHAPE;
		if (facing == Direction.WEST)
			return WEST_SHAPE;
		if (facing == Direction.NORTH)
			return NORTH_SHAPE;
		if (facing == Direction.SOUTH)
			return SOUTH_SHAPE;

		return VoxelShapes.empty();
	}

	private void cacheItemPositions() {
		if (!itemPositions.isEmpty())
			return;

		Vec3d first = Vec3d.ZERO;
		Vec3d second = Vec3d.ZERO;
		Vec3d shift = VecHelper.getCenterOf(BlockPos.ZERO);
		float zFightOffset = 1 / 128f;

		for (Direction facing : Direction.values()) {
			if (facing.getAxis().isHorizontal()) {
				first = new Vec3d(10 / 16f, 5.5f / 16f, 2f / 16f + zFightOffset);
				second = new Vec3d(10 / 16f, 10.5f / 16f, 2f / 16f + zFightOffset);

				float angle = facing.getHorizontalAngle();
				if (facing.getAxis() == Axis.X)
					angle = -angle;

				first = VecHelper.rotate(first.subtract(shift), angle, Axis.Y).add(shift);
				second = VecHelper.rotate(second.subtract(shift), angle, Axis.Y).add(shift);

			} else {
				first = new Vec3d(10 / 16f, 2f / 16f + zFightOffset, 5.5f / 16f);
				second = new Vec3d(10 / 16f, 2f / 16f + zFightOffset, 10.5f / 16f);

				if (facing == Direction.DOWN) {
					first = VecHelper.rotate(first.subtract(shift), 180, Axis.X).add(shift);
					second = VecHelper.rotate(second.subtract(shift), 180, Axis.X).add(shift);
				}
			}

			itemPositions.add(Pair.of(first, second));
		}

	}

	public Pair<Vec3d, Vec3d> getFrequencyItemPositions(BlockState state) {
		Direction facing = state.get(FACING);
		return itemPositions.get(facing.getIndex());
	}

	@Override
	public Direction getFrequencyItemFacing(BlockState state) {
		return state.get(FACING);
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

}
