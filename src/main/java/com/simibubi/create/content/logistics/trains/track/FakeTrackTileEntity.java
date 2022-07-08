package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FakeTrackTileEntity extends SyncedTileEntity {

	int keepAlive;
	
	public FakeTrackTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		keepAlive();
	}
	
	public void randomTick() {
		keepAlive--;
		if (keepAlive > 0)
			return;
		level.removeBlock(worldPosition, false);
	}
	
	public void keepAlive() {
		keepAlive = 3;
	}
	

}
