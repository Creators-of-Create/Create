package com.simibubi.create.content.contraptions.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class InterPumpFluidHandler extends FluidTank {

	InterPumpEndpoint endpoint;

	public InterPumpFluidHandler(InterPumpEndpoint endpoint) {
		super(Integer.MAX_VALUE);
		this.endpoint = endpoint;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return 0;
		int maxInput = Math.min(resource.getAmount(), Math.max(getTransferCapacity() - getFluidAmount(), 0));
		FluidStack toInsert = resource.copy();
		toInsert.setAmount(maxInput);
		FluidPropagator.showBlockFace(endpoint.location).colored(0x77d196).lineWidth(1/4f);
		return super.fill(toInsert, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return super.drain(maxDrain, action);
	}
	
	public FluidStack provide() {
		FluidStack heldFluid = getFluid();
		if (heldFluid.isEmpty())
			return heldFluid;
		FluidStack copy = heldFluid.copy();
		copy.setAmount(1);
		return copy;
	}
	
	private int getTransferCapacity() {
		return Math.min(endpoint.getTransferSpeed(true), endpoint.getTransferSpeed(false));
	}

}
