package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.foundation.utility.LegacyDirectionBridge;

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
		return LegacyDirectionBridge.nearest(localSource.getX(), localSource.getY(), localSource.getZ(), Direction.NORTH);
	}

}
