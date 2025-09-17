package com.simibubi.create.content.redstone.deskBell;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeskBellBlock extends WrenchableDirectionalBlock
	implements ProperWaterloggedBlock, IBE<DeskBellBlockEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public DeskBellBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP)
			.setValue(POWERED, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withWater(defaultBlockState().setValue(FACING, context.getClickedFace()), context);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pPos);
		return pState;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.DESK_BELL.get(pState.getValue(FACING));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED, WATERLOGGED));
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		playSound(player, level, pos);
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
		updateNeighbours(state, level, pos);
		withBlockEntityDo(level, pos, DeskBellBlockEntity::ding);
		return InteractionResult.SUCCESS;
	}

	public void playSound(@Nullable Player pPlayer, LevelAccessor pLevel, BlockPos pPos) {
		if (pLevel instanceof Level level)
			AllSoundEvents.DESK_BELL_USE.play(level, pPlayer, pPos);
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (!pIsMoving && !pState.is(pNewState.getBlock()))
			if (pState.getValue(POWERED))
				updateNeighbours(pState, pLevel, pPos);
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return pBlockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return pBlockState.getValue(POWERED) && getConnectedDirection(pBlockState) == pSide ? 15 : 0;
	}

	@Override
	public boolean isSignalSource(BlockState pState) {
		return true;
	}

	public void unPress(BlockState pState, Level pLevel, BlockPos pPos) {
		pLevel.setBlock(pPos, pState.setValue(POWERED, false), Block.UPDATE_ALL);
		updateNeighbours(pState, pLevel, pPos);
	}

	protected void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
		pLevel.updateNeighborsAt(pPos, this);
		pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), this);
	}

	private Direction getConnectedDirection(BlockState pState) {
		return pState.getOptionalValue(FACING)
			.orElse(Direction.UP);
	}

	@Override
	public Class<DeskBellBlockEntity> getBlockEntityClass() {
		return DeskBellBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends DeskBellBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.DESK_BELL.get();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

}
