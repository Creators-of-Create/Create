package com.simibubi.create.lib.transfer.fluid;

import net.minecraft.nbt.CompoundTag;

public class FluidTank implements IFluidHandler {
	protected FluidStack fluid = FluidStack.empty();
	protected long capacity;

	public FluidTank(FluidStack fluid, long capacity) {
		this(capacity);
		this.fluid = fluid;
	}

	public FluidTank(long capacity) {
		this.capacity = capacity;
	}

	public FluidTank setCapacity(long capacity) {
		this.capacity = capacity;
		return this;
	}

	public long getCapacity() {
		return capacity;
	}

	public FluidStack getFluid() {
		return fluid;
	}

	public void setFluid(FluidStack fluid) {
		this.fluid = fluid;
	}

	public CompoundTag writeToNBT(CompoundTag tag) {
		fluid.writeToNBT(tag);
		tag.putLong("Capacity", capacity);
		return tag;
	}

	public FluidTank readFromNBT(CompoundTag tag) {
		FluidStack stack = FluidStack.loadFluidStackFromNBT(tag);
		long capacity = tag.getLong("Capacity");
		return new FluidTank(stack, capacity);
	}

	public boolean isEmpty() {
		return getFluid() == null || getFluid().getAmount() == 0;
	}

	public long getFluidAmount() {
		return getFluid().getAmount();
	}

	public long getSpace() {
		return Math.max(0, capacity - getFluid().getAmount());
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return fluid;
	}

	@Override
	public long getTankCapacity(int tank) {
		return capacity;
	}

	@Override
	public long fill(FluidStack resource, boolean sim) {
		if (resource.isEmpty() || !isFluidValid(0, resource)) {
			return 0;
		}
		if (sim) {
			if (fluid.isEmpty()) {
				return Math.min(capacity, resource.getAmount());
			}
			if (!fluid.isFluidEqual(resource)) {
				return 0;
			}
			return Math.min(capacity - fluid.getAmount(), resource.getAmount());
		}
		if (fluid.isEmpty()) {
			fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
			onContentsChanged();
			return fluid.getAmount();
		}
		if (!fluid.isFluidEqual(resource)) {
			return 0;
		}
		long filled = capacity - fluid.getAmount();

		if (resource.getAmount() < filled) {
			fluid.grow(resource.getAmount());
			filled = resource.getAmount();
		}
		else {
			fluid.setAmount(capacity);
		}
		if (filled > 0)
			onContentsChanged();
		return filled;
	}

	@Override
	public FluidStack drain(FluidStack stack, boolean sim) {
		return drain(stack.getAmount(), sim);
	}

	@Override
	public FluidStack drain(long amount, boolean sim) {
		long canRemove = fluid.getAmount();
		if (amount > canRemove) amount = canRemove;
		FluidStack out = fluid.copy().setAmount(amount);
		if (!sim) {
			fluid.setAmount(fluid.getAmount() - amount);
			onContentsChanged();
		}

		return out;
	}

	protected void onContentsChanged() {

	}
}
