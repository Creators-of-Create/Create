package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RedstoneLinkBlock extends ProperDirectionalBlock implements ITE<RedstoneLinkTileEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty RECEIVER = BooleanProperty.create("receiver");

	public RedstoneLinkBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(POWERED, false)
			.with(RECEIVER, false));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = state.get(FACING);
		if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}

		if (!worldIn.getPendingBlockTicks()
			.isTickPending(pos, this))
			worldIn.getPendingBlockTicks()
				.scheduleTick(pos, this, 0);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
		updateTransmittedSignal(state, worldIn, pos);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() == oldState.getBlock() || isMoving)
			return;
		updateTransmittedSignal(state, worldIn, pos);
	}

	public void updateTransmittedSignal(BlockState state, World worldIn, BlockPos pos) {
		if (worldIn.isRemote)
			return;
		if (state.get(RECEIVER))
			return;

		int power = getPower(worldIn, pos);

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != power > 0)
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);

		int transmit = power;
		withTileEntityDo(worldIn, pos, te -> te.transmit(transmit));
	}

	private int getPower(World worldIn, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getRedstonePower(pos.offset(direction), direction), power);
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getRedstonePower(pos.offset(direction), Direction.UP), power);
		return power;
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
		try {
			RedstoneLinkTileEntity tileEntity = getTileEntity(blockAccess, pos);
			return tileEntity.getReceivedSignal();
		} catch (TileEntityException e) {
		}
		return 0;
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
		return AllTileEntities.REDSTONE_LINK.create();
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (player.isSneaking())
			return toggleMode(state, worldIn, pos);
		return ActionResultType.PASS;
	}

	public ActionResultType toggleMode(BlockState state, World worldIn, BlockPos pos) {
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;
		try {
			RedstoneLinkTileEntity te = getTileEntity(worldIn, pos);
			Boolean wasReceiver = state.get(RECEIVER);
			boolean blockPowered = worldIn.isBlockPowered(pos);
			worldIn.setBlockState(pos, state.cycle(RECEIVER)
				.with(POWERED, blockPowered), 3);
			te.transmit(wasReceiver ? 0 : getPower(worldIn, pos));
			return ActionResultType.SUCCESS;
		} catch (TileEntityException e) {
		}
		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (toggleMode(state, context.getWorld(), context.getPos()) == ActionResultType.SUCCESS)
			return ActionResultType.SUCCESS;
		return super.onWrenched(state, context);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction _targetedFace) {
		return originalState;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.offset(state.get(FACING)
			.getOpposite());
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return !neighbour.getMaterial()
			.isReplaceable();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();
		state = state.with(FACING, context.getFace());
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.REDSTONE_BRIDGE.get(state.get(FACING));
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	public Class<RedstoneLinkTileEntity> getTileEntityClass() {
		return RedstoneLinkTileEntity.class;
	}

}
