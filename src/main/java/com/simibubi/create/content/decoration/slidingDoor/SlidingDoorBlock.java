package com.simibubi.create.content.decoration.slidingDoor;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import com.simibubi.create.foundation.block.IHaveBigOutline;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;

public class SlidingDoorBlock extends DoorBlock implements IWrenchable, IBE<SlidingDoorBlockEntity>, IHaveBigOutline {

	public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");
	private boolean folds;

	@Deprecated // Remove in 1.19 - Fixes incompatibility with Quarks double door module
	public static void stopItQuark(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getPlayer();
		Level world = event.getWorld();

		if (!world.isClientSide || player.isDiscrete() || event.isCanceled() || event.getResult() == Result.DENY
			|| event.getUseBlock() == Result.DENY)
			return;

		BlockPos pos = event.getPos();
		BlockState blockState = world.getBlockState(pos);

		if (blockState.getBlock() instanceof SlidingDoorBlock sdb) {
			event.setCanceled(true);
			event.setCancellationResult(blockState.use(world, player, event.getHand(), event.getHitVec()));
		}
	}

	public SlidingDoorBlock(Properties p_52737_, boolean folds) {
		super(p_52737_);
		this.folds = folds;
	}

	public boolean isFoldingDoor() {
		return folds;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(VISIBLE));
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (!pState.getValue(OPEN) && (pState.getValue(VISIBLE) || pLevel instanceof ContraptionWorld))
			return super.getShape(pState, pLevel, pPos, pContext);

		Direction direction = pState.getValue(FACING);
		boolean hinge = pState.getValue(HINGE) == DoorHingeSide.RIGHT;
		return SlidingDoorShapes.get(direction, hinge, isFoldingDoor());
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return pState.getValue(HALF) == DoubleBlockHalf.LOWER || pLevel.getBlockState(pPos.below())
			.is(this);
	}

	@Override
	public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return getShape(pState, pLevel, pPos, CollisionContext.empty());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState stateForPlacement = super.getStateForPlacement(pContext);
		if (stateForPlacement != null && stateForPlacement.getValue(OPEN))
			return stateForPlacement.setValue(OPEN, false)
				.setValue(POWERED, false);
		return stateForPlacement;
	}

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		if (!pOldState.is(this))
			deferUpdate(pLevel, pPos);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
		BlockPos pCurrentPos, BlockPos pFacingPos) {
		BlockState blockState = super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
		if (blockState.isAir())
			return blockState;
		DoubleBlockHalf doubleblockhalf = blockState.getValue(HALF);
		if (pFacing.getAxis() == Direction.Axis.Y
			&& doubleblockhalf == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
			return pFacingState.is(this) && pFacingState.getValue(HALF) != doubleblockhalf
				? blockState.setValue(VISIBLE, pFacingState.getValue(VISIBLE))
				: Blocks.AIR.defaultBlockState();
		}
		return blockState;
	}

	@Override
	public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
		if (!state.is(this))
			return;
		if (state.getValue(OPEN) == open)
			return;
		BlockState changedState = state.setValue(OPEN, open);
		if (open)
			changedState = changedState.setValue(VISIBLE, false);
		level.setBlock(pos, changedState, 10);

		DoorHingeSide hinge = changedState.getValue(HINGE);
		Direction facing = changedState.getValue(FACING);
		BlockPos otherPos =
			pos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
		BlockState otherDoor = level.getBlockState(otherPos);
		if (isDoubleDoor(changedState, hinge, facing, otherDoor))
			setOpen(entity, level, otherDoor, otherPos, open);

		this.playSound(level, pos, open);
		level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
	}

	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
		boolean pIsMoving) {
		boolean lower = pState.getValue(HALF) == DoubleBlockHalf.LOWER;
		boolean isPowered = isDoorPowered(pLevel, pPos, pState);
		if (defaultBlockState().is(pBlock))
			return;
		if (isPowered == pState.getValue(POWERED))
			return;

		SlidingDoorBlockEntity be = getBlockEntity(pLevel, lower ? pPos : pPos.below());
		if (be != null && be.deferUpdate)
			return;

		BlockState changedState = pState.setValue(POWERED, Boolean.valueOf(isPowered))
			.setValue(OPEN, Boolean.valueOf(isPowered));
		if (isPowered)
			changedState = changedState.setValue(VISIBLE, false);

		if (isPowered != pState.getValue(OPEN)) {
			this.playSound(pLevel, pPos, isPowered);
			pLevel.gameEvent(isPowered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);

			DoorHingeSide hinge = changedState.getValue(HINGE);
			Direction facing = changedState.getValue(FACING);
			BlockPos otherPos =
				pPos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
			BlockState otherDoor = pLevel.getBlockState(otherPos);

			if (isDoubleDoor(changedState, hinge, facing, otherDoor)) {
				otherDoor = otherDoor.setValue(POWERED, Boolean.valueOf(isPowered))
					.setValue(OPEN, Boolean.valueOf(isPowered));
				if (isPowered)
					otherDoor = otherDoor.setValue(VISIBLE, false);
				pLevel.setBlock(otherPos, otherDoor, 2);
			}
		}

		pLevel.setBlock(pPos, changedState, 2);
	}

	public static boolean isDoorPowered(Level pLevel, BlockPos pPos, BlockState state) {
		boolean lower = state.getValue(HALF) == DoubleBlockHalf.LOWER;
		DoorHingeSide hinge = state.getValue(HINGE);
		Direction facing = state.getValue(FACING);
		BlockPos otherPos =
			pPos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
		BlockState otherDoor = pLevel.getBlockState(otherPos);

		if (isDoubleDoor(state.cycle(OPEN), hinge, facing, otherDoor) && (pLevel.hasNeighborSignal(otherPos)
			|| pLevel.hasNeighborSignal(otherPos.relative(lower ? Direction.UP : Direction.DOWN))))
			return true;

		return pLevel.hasNeighborSignal(pPos)
			|| pLevel.hasNeighborSignal(pPos.relative(lower ? Direction.UP : Direction.DOWN));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {

		pState = pState.cycle(OPEN);
		if (pState.getValue(OPEN))
			pState = pState.setValue(VISIBLE, false);
		pLevel.setBlock(pPos, pState, 10);
		pLevel.gameEvent(pPlayer, isOpen(pState) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);

		DoorHingeSide hinge = pState.getValue(HINGE);
		Direction facing = pState.getValue(FACING);
		BlockPos otherPos =
			pPos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
		BlockState otherDoor = pLevel.getBlockState(otherPos);
		if (isDoubleDoor(pState, hinge, facing, otherDoor))
			use(otherDoor, pLevel, otherPos, pPlayer, pHand, pHit);
		else if (pState.getValue(OPEN))
			pLevel.levelEvent(pPlayer, getOpenSound(), pPos, 0);

		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}

	public void deferUpdate(LevelAccessor level, BlockPos pos) {
		withBlockEntityDo(level, pos, sdte -> sdte.deferUpdate = true);
	}

	public static boolean isDoubleDoor(BlockState pState, DoorHingeSide hinge, Direction facing, BlockState otherDoor) {
		return otherDoor.getBlock() == pState.getBlock() && otherDoor.getValue(HINGE) != hinge
			&& otherDoor.getValue(FACING) == facing && otherDoor.getValue(OPEN) != pState.getValue(OPEN)
			&& otherDoor.getValue(HALF) == pState.getValue(HALF);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return pState.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
	}

	private void playSound(Level pLevel, BlockPos pPos, boolean pIsOpening) {
		if (pIsOpening)
			pLevel.levelEvent((Player) null, this.getOpenSound(), pPos, 0);
	}

	private int getOpenSound() {
		return 1005;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER)
			return null;
		return IBE.super.newBlockEntity(pos, state);
	}

	@Override
	public Class<SlidingDoorBlockEntity> getBlockEntityClass() {
		return SlidingDoorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends SlidingDoorBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.SLIDING_DOOR.get();
	}

}
