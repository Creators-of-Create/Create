package com.simibubi.create.foundation.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class SmartBlockEntityTicker<T extends BlockEntity> implements BlockEntityTicker<T> {

	@Override
	public void tick(Level p_155253_, BlockPos p_155254_, BlockState p_155255_, T p_155256_) {
		if (!p_155256_.hasLevel())
			p_155256_.setLevel(p_155253_);
		((SmartBlockEntity) p_155256_).tick();
	}

}
