package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import java.util.Random;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SignalBlock extends Block implements SimpleWaterloggedBlock, ITE<SignalTileEntity> {

	public SignalBlock(Properties p_53182_) {
		super(p_53182_);
	}

	@Override
	public Class<SignalTileEntity> getTileEntityClass() {
		return SignalTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends SignalTileEntity> getTileEntityType() {
		return AllTileEntities.TRACK_SIGNAL.get();
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel world, BlockPos pos, Random random) {
		getTileEntityOptional(world, pos).ifPresent(SignalTileEntity::updatePowerAfterDelay);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return getTileEntityOptional(blockAccess, pos).filter(SignalTileEntity::isPowered)
			.map($ -> 15)
			.orElse(0);
	}

}
