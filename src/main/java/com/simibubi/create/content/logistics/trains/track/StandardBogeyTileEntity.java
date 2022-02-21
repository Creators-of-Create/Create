package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.foundation.tileEntity.CachedRenderBBTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class StandardBogeyTileEntity extends CachedRenderBBTileEntity {

	public StandardBogeyTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(2);
	}

}
