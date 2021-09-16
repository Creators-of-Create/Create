package com.simibubi.create.content.contraptions.fluids.pipes;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction.Axis;

public interface IAxisPipe {

	@Nullable
	public static Axis getAxisOf(BlockState state) {
		if (state.getBlock() instanceof IAxisPipe) 
			return ((IAxisPipe) state.getBlock()).getAxis(state);
		return null;
	}

	public Axis getAxis(BlockState state);

}
