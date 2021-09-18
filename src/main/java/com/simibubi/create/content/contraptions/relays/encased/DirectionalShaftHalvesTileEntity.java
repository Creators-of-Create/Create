package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalShaftHalvesTileEntity extends KineticTileEntity {

	public DirectionalShaftHalvesTileEntity(BlockPos pos, BlockState state, BlockEntityType<?> type) {
		super(type, pos, state);
	}

	public Direction getSourceFacing() {
		BlockPos localSource = source.subtract(getBlockPos());
		return Direction.getNearest(localSource.getX(), localSource.getY(), localSource.getZ());
	}

}
