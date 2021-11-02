package com.simibubi.create.foundation.tileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class SmartTileEntityTicker<T extends SmartTileEntity> implements BlockEntityTicker<T> {

	@Override
	public void tick(Level p_155253_, BlockPos p_155254_, BlockState p_155255_, T p_155256_) {
		if (!p_155256_.hasLevel())
			p_155256_.setLevel(p_155253_);
		p_155256_.tick();
	}

}
