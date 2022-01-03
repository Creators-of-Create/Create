package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.KineticConnections;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ClutchTileEntity extends KineticTileEntity {

	public ClutchTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public KineticConnections getConnections() {
		BlockState state = getBlockState();
		Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

		if (!state.getValue(BlockStateProperties.POWERED)) return AllConnections.FULL_SHAFT.apply(axis);

		return getSpeedSource()
				.map(p -> AllConnections.HALF_SHAFT.apply(Direction.fromNormal(p.subtract(getBlockPos()))))
				.orElse(AllConnections.FULL_SHAFT.apply(axis));
	}

}
