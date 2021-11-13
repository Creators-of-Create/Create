package com.simibubi.create.lib.block;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface CreateBlockEntity {
	BlockEntityTicker<?> CREATE_TICKER = (BlockEntityTicker<BlockEntity>) (level, blockPos, blockState, blockEntity) -> {
		if (!level.isClientSide() && blockEntity instanceof CreateBlockEntity be)
			be.tick();
	};

	default void tick() {}
}
