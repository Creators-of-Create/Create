package com.simibubi.create.content.contraptions.relays.gearbox;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import com.simibubi.create.content.contraptions.relays.encased.GearshiftBlock;
import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.ConnectionsBuilder;
import com.simibubi.create.content.contraptions.solver.KineticConnections;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GearshiftTileEntity extends KineticTileEntity {

	public GearshiftTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public KineticConnections getConnections() {
		BlockState state = getBlockState();
		Direction dir = AllConnections.pos(state.getValue(GearshiftBlock.AXIS));

		ConnectionsBuilder builder = ConnectionsBuilder.builder();
		if (!state.getValue(BlockStateProperties.POWERED))
			return builder.withFullShaft(dir.getAxis()).build();
		return builder
				.withHalfShaft(dir, -1)
				.withHalfShaft(dir.getOpposite(), 1)
				.build();
	}

}
