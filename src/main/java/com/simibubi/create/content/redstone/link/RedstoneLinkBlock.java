package com.simibubi.create.content.redstone.link;

import java.util.Random;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import net.createmod.catnip.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedstoneLinkBlock extends WrenchableDirectionalBlock implements IBE<RedstoneLinkBlockEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty RECEIVER = BooleanProperty.create("receiver");

	public RedstoneLinkBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(RECEIVER, false));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
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
			worldIn.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random r) {
		updateTransmittedSignal(state, worldIn, pos);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() == oldState.getBlock() || isMoving)
			return;
		updateTransmittedSignal(state, worldIn, pos);
	}

	public void updateTransmittedSignal(BlockState state, Level worldIn, BlockPos pos) {
		if (worldIn.isClientSide)
			return;
		if (state.getValue(RECEIVER))
			return;

		int power = getPower(worldIn, pos);

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != power > 0)
			worldIn.setBlock(pos, state.cycle(POWERED), 2);

		int transmit = power;
		withBlockEntityDo(worldIn, pos, be -> be.transmit(transmit));
	}

	private int getPower(Level worldIn, BlockPos pos) {
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
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (side != blockState.getValue(FACING))
			return 0;
		return getSignal(blockState, blockAccess, pos, side);
	}

	@Override
	public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (!state.getValue(RECEIVER))
			return 0;
		return getBlockEntityOptional(blockAccess, pos).map(RedstoneLinkBlockEntity::getReceivedSignal)
				.orElse(0);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, RECEIVER);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (player.isShiftKeyDown())
			return toggleMode(state, worldIn, pos);
		return InteractionResult.PASS;
	}

	public InteractionResult toggleMode(BlockState state, Level worldIn, BlockPos pos) {
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;

		return onBlockEntityUse(worldIn, pos, be -> {
			Boolean wasReceiver = state.getValue(RECEIVER);
			boolean blockPowered = worldIn.hasNeighborSignal(pos);
			worldIn.setBlock(pos, state.cycle(RECEIVER)
					.setValue(POWERED, blockPowered), 3);
			be.transmit(wasReceiver ? 0 : getPower(worldIn, pos));
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (toggleMode(state, context.getLevel(), context.getClickedPos()) == InteractionResult.SUCCESS)
			return InteractionResult.SUCCESS;
		return super.onWrenched(state, context);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction _targetedFace) {
		return originalState;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.relative(state.getValue(FACING)
			.getOpposite());
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return !neighbour.getMaterial()
			.isReplaceable();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState();
		state = state.setValue(FACING, context.getClickedFace());
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.REDSTONE_BRIDGE.get(state.getValue(FACING));
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	public Class<RedstoneLinkBlockEntity> getBlockEntityClass() {
		return RedstoneLinkBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends RedstoneLinkBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.REDSTONE_LINK.get();
	}

}
