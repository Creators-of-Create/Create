package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
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

public class RedstoneLinkBlock extends WrenchableDirectionalBlock implements ITE<RedstoneLinkTileEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty RECEIVER = BooleanProperty.create("receiver");

	public RedstoneLinkBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(RECEIVER, false));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		Direction blockFacing = state.getValue(FACING);
		if (fromPos.equals(pos.relative(blockFacing.getOpposite()))) {
			if (!canSurvive(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}

		if (!worldIn.getBlockTicks()
			.willTickThisTick(pos, this))
			worldIn.getBlockTicks()
				.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
		updateTransmittedSignal(state, worldIn, pos);
	}

	@Override
	public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() == oldState.getBlock() || isMoving)
			return;
		updateTransmittedSignal(state, worldIn, pos);
	}

	public void updateTransmittedSignal(BlockState state, World worldIn, BlockPos pos) {
		if (worldIn.isClientSide)
			return;
		if (state.getValue(RECEIVER))
			return;

		int power = getPower(worldIn, pos);

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != power > 0)
			worldIn.setBlock(pos, state.cycle(POWERED), 2);

		int transmit = power;
		withTileEntityDo(worldIn, pos, te -> te.transmit(transmit));
	}

	private int getPower(World worldIn, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getSignal(pos.relative(direction), direction), power);
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getSignal(pos.relative(direction), Direction.UP), power);
		return power;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return state.getValue(POWERED) && state.getValue(RECEIVER);
	}

	@Override
	public int getDirectSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (side != blockState.getValue(FACING))
			return 0;
		return getSignal(blockState, blockAccess, pos, side);
	}

	@Override
	public int getSignal(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (!state.getValue(RECEIVER))
			return 0;
		return getTileEntityOptional(blockAccess, pos).map(RedstoneLinkTileEntity::getReceivedSignal)
				.orElse(0);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, RECEIVER);
		super.createBlockStateDefinition(builder);
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
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (player.isShiftKeyDown())
			return toggleMode(state, worldIn, pos);
		return ActionResultType.PASS;
	}

	public ActionResultType toggleMode(BlockState state, World worldIn, BlockPos pos) {
		if (worldIn.isClientSide)
			return ActionResultType.SUCCESS;

		return onTileEntityUse(worldIn, pos, te -> {
			Boolean wasReceiver = state.getValue(RECEIVER);
			boolean blockPowered = worldIn.hasNeighborSignal(pos);
			worldIn.setBlock(pos, state.cycle(RECEIVER)
					.setValue(POWERED, blockPowered), 3);
			te.transmit(wasReceiver ? 0 : getPower(worldIn, pos));
			return ActionResultType.SUCCESS;
		});
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (toggleMode(state, context.getLevel(), context.getClickedPos()) == ActionResultType.SUCCESS)
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
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.relative(state.getValue(FACING)
			.getOpposite());
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return !neighbour.getMaterial()
			.isReplaceable();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = defaultBlockState();
		state = state.setValue(FACING, context.getClickedFace());
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.REDSTONE_BRIDGE.get(state.getValue(FACING));
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	public Class<RedstoneLinkTileEntity> getTileEntityClass() {
		return RedstoneLinkTileEntity.class;
	}

}
