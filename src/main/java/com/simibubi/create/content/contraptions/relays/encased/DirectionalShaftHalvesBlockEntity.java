package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalShaftHalvesBlockEntity extends KineticBlockEntity {

	public DirectionalShaftHalvesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Direction getSourceFacing() {
		BlockPos localSource = source.subtract(getBlockPos());
		return Direction.getNearest(localSource.getX(), localSource.getY(), localSource.getZ());
	}

}
