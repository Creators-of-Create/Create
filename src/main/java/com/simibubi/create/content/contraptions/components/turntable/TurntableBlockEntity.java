package com.simibubi.create.content.contraptions.components.turntable;

import com.simibubi.create.content.contraptions.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TurntableBlockEntity extends KineticBlockEntity {

	public TurntableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

}
