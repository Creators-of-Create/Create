package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.KineticConnections;
import com.simibubi.create.content.contraptions.solver.KineticNodeState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SimpleKineticTileEntity extends KineticTileEntity {

	public SimpleKineticTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public AABB makeRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public KineticNodeState getInitialKineticNodeState() {
		KineticConnections connections = AllConnections.EMPTY;
		BlockState state = getBlockState();
		if (state.getBlock() instanceof ISimpleConnectable connectable) {
			connections = connectable.getConnections(state);
		}
		return new KineticNodeState(connections, 0);
	}

}
