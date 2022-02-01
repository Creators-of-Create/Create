package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class StandardBogeyTileEntity extends SyncedTileEntity {

	public StandardBogeyTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public AABB getRenderBoundingBox() {
		return super.getRenderBoundingBox().inflate(2);
	}

}
