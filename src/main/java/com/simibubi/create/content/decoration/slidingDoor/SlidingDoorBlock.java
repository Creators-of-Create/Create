package com.simibubi.create.content.decoration.slidingDoor;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlidingDoorBlock extends DoorBlock implements IWrenchable, IBE<SlidingDoorBlockEntity>, IHaveBigOutline {
	public static final Supplier<BlockSetType> TRAIN_SET_TYPE =
		() -> new BlockSetType("create:train", true, true, true,
			BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.NETHERITE_BLOCK, SoundEvents.IRON_DOOR_CLOSE,
			SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
			SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
			SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

	public static final Supplier<BlockSetType> GLASS_SET_TYPE =
		() -> new BlockSetType("create:glass", true, true, true,
			BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.GLASS, SoundEvents.IRON_DOOR_CLOSE,
			SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
			SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
			SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

	public static final Supplier<BlockSetType> STONE_SET_TYPE =
		() -> new BlockSetType("create:stone", true, true, true,
			BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.STONE, SoundEvents.IRON_DOOR_CLOSE,
			SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
			SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
			SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

	public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");
	private final boolean folds;

	public static SlidingDoorBlock metal(Properties properties, boolean folds) {
		return new SlidingDoorBlock(properties, TRAIN_SET_TYPE.get(), folds);
	}

	public static SlidingDoorBlock glass(Properties properties, boolean folds) {
		return new SlidingDoorBlock(properties, GLASS_SET_TYPE.get(), folds);
	}

	public static SlidingDoorBlock stone(Properties properties, boolean folds) {
		return new SlidingDoorBlock(properties, STONE_SET_TYPE.get(), folds);
	}

	public SlidingDoorBlock(Properties properties, BlockSetType type, boolean folds) {
		super(type, properties);
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
		level.setBlock(pos, changedState, UPDATE_CLIENTS | UPDATE_IMMEDIATE);

		DoorHingeSide hinge = changedState.getValue(HINGE);
		Direction facing = changedState.getValue(FACING);
		BlockPos otherPos =
			pos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
		BlockState otherDoor = level.getBlockState(otherPos);
		if (isDoubleDoor(changedState, hinge, facing, otherDoor))
			setOpen(entity, level, otherDoor, otherPos, open);

		this.playSound(entity, level, pos, open);
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

		BlockState changedState = pState.setValue(POWERED, isPowered)
			.setValue(OPEN, isPowered);
		if (isPowered)
			changedState = changedState.setValue(VISIBLE, false);

		if (isPowered != pState.getValue(OPEN)) {
			this.playSound(null, pLevel, pPos, isPowered);
			pLevel.gameEvent(null, isPowered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);

			DoorHingeSide hinge = changedState.getValue(HINGE);
			Direction facing = changedState.getValue(FACING);
			BlockPos otherPos =
				pPos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
			BlockState otherDoor = pLevel.getBlockState(otherPos);

			if (isDoubleDoor(changedState, hinge, facing, otherDoor)) {
				otherDoor = otherDoor.setValue(POWERED, isPowered)
					.setValue(OPEN, isPowered);
				if (isPowered)
					otherDoor = otherDoor.setValue(VISIBLE, false);
				pLevel.setBlock(otherPos, otherDoor, Block.UPDATE_CLIENTS);
			}
		}

		pLevel.setBlock(pPos, changedState, Block.UPDATE_CLIENTS);
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
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		state = state.cycle(OPEN);
		boolean isOpen = state.getValue(OPEN);
		if (isOpen)
			state = state.setValue(VISIBLE, false);
		level.setBlock(pos, state, UPDATE_CLIENTS | UPDATE_IMMEDIATE);
		level.gameEvent(player, isOpen(state) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

		DoorHingeSide hinge = state.getValue(HINGE);
		Direction facing = state.getValue(FACING);
		BlockPos otherPos =
			pos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
		BlockState otherDoor = level.getBlockState(otherPos);
		if (isDoubleDoor(state, hinge, facing, otherDoor))
			useWithoutItem(otherDoor, level, otherPos, player, hitResult);
		else if (isOpen) {
			this.playSound(player, level, pos, true);
			level.gameEvent(player, GameEvent.BLOCK_OPEN, pos);
		}

		return InteractionResult.sidedSuccess(level.isClientSide);
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

	private void playSound(@Nullable Entity pSource, Level pLevel, BlockPos pPos, boolean pIsOpening) {
		pLevel.playSound(pSource, pPos, pIsOpening ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
			SoundSource.BLOCKS, 1.0F, pLevel.getRandom()
				.nextFloat() * 0.1F + 0.9F);
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
