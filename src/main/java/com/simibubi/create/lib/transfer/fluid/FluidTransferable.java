package com.simibubi.create.lib.transfer.fluid;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;

public interface FluidTransferable {
	@Nullable
	IFluidHandler getFluidHandler(@Nullable Direction direction);
}
