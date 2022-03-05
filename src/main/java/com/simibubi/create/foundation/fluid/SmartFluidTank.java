package com.simibubi.create.foundation.fluid;

import java.util.function.Consumer;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;

public class SmartFluidTank extends FluidTank {

	private Consumer<FluidStack> updateCallback;

	public SmartFluidTank(long capacity, Consumer<FluidStack> updateCallback) {
		super(capacity);
		this.updateCallback = updateCallback;
	}

	@Override
	protected void onContentsChanged() {
		super.onContentsChanged();
		updateCallback.accept(getFluid());
	}

	@Override
	public void setFluid(FluidStack stack) {
		super.setFluid(stack);
		updateCallback.accept(stack);
	}


	@Override
	public long fill(FluidStack resource, boolean sim) {
		long val = super.fill(resource, sim);
		onContentsChanged();
		return val;
	}

	@Override
	public FluidStack drain(long amount, boolean sim) {
		FluidStack val = super.drain(amount, sim);
		onContentsChanged();
		return val;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean sim) {
		FluidStack val = super.drain(resource, sim);
		onContentsChanged();
		return val;
	}
}
