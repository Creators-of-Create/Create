package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.foundation.utility.BlockFace;

import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

public class PumpEndpoint extends FluidNetworkEndpoint {

	PumpTileEntity pumpTE;

	public PumpEndpoint(BlockFace location, PumpTileEntity pumpTE) {
		super(pumpTE.getWorld(), location, LazyOptional.empty());
		this.pumpTE = pumpTE;
	}

	@Override
	protected void onHandlerInvalidated(IWorld world) {}

	@Override
	public FluidStack provideFluid() {
		return pumpTE.providedFluid;
	}

}