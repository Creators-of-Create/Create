package com.simibubi.create.lib.transfer.fluid;

public class EmptyTank extends FluidTank {
	public static final EmptyTank INSTANCE = new EmptyTank();

	private EmptyTank() {
		super(FluidStack.EMPTY, 0);
	}

	@Override
	public FluidTank setCapacity(long capacity) {
		return this;
	}

	@Override
	public void setFluid(FluidStack fluid) {
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}
