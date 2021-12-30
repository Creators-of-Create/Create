package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

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

}
