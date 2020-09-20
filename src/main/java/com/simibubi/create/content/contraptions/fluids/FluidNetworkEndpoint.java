package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.foundation.utility.BlockFace;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidNetworkEndpoint {
	BlockFace location;
	protected LazyOptional<IFluidHandler> handler;

	public FluidNetworkEndpoint(IWorld world, BlockFace location, LazyOptional<IFluidHandler> handler) {
		this.location = location;
		this.handler = handler;
		this.handler.addListener($ -> onHandlerInvalidated(world));
	}

	protected void onHandlerInvalidated(IWorld world) {
		IFluidHandler tank = handler.orElse(null);
		if (tank != null)
			return;
		TileEntity tileEntity = world.getTileEntity(location.getConnectedPos());
		if (tileEntity == null)
			return;
		LazyOptional<IFluidHandler> capability =
			tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, location.getOppositeFace());
		if (capability.isPresent()) {
			handler = capability;
			handler.addListener($ -> onHandlerInvalidated(world));
		}
	}

	public FluidStack provideFluid() {
		IFluidHandler tank = provideHandler().orElse(null);
		if (tank == null)
			return FluidStack.EMPTY;
		return tank.drain(1, FluidAction.SIMULATE);
	}

	public LazyOptional<IFluidHandler> provideHandler() {
		return handler;
	}

}
