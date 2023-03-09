package com.simibubi.create.content.logistics.trains.management.edgePoint.observer;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class TrackObserverBlock extends Block implements ITE<TrackObserverTileEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public TrackObserverBlock(Properties p_49795_) {
		super(p_49795_);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(POWERED));
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return true;
	}

	@Override
	public Class<TrackObserverTileEntity> getTileEntityClass() {
		return TrackObserverTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends TrackObserverTileEntity> getTileEntityType() {
		return AllTileEntities.TRACK_OBSERVER.get();
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		ITE.onRemove(state, worldIn, pos, newState);
	}

}
