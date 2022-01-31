package com.simibubi.create.lib.transfer.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/**
 * Wraps a Storage in an IFluidHandler, for use in Create
 */
@SuppressWarnings({"UnstableApiUsage"})
public class FluidStorageHandler implements IFluidHandler {
	protected final Storage<FluidVariant> storage;

	public FluidStorageHandler(Storage<FluidVariant> storage) {
		this.storage = storage;
	}

	@Override
	public int getTanks() {
		int tanks = 0;
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<FluidVariant> view : storage.iterable(t)) {
				tanks++;
			}
			t.abort();
		}
		return tanks;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		try (Transaction t = Transaction.openOuter()) {
			int index = 0;
			for (StorageView<FluidVariant> view : storage.iterable(t)) {
				if (index == tank) {
					return new FluidStack(view.getResource(), view.getAmount());
				}
				index++;
			}
			t.abort();
		}
		return FluidStack.EMPTY;
	}

	@Override
	public long getTankCapacity(int tank) {
		try (Transaction t = Transaction.openOuter()) {
			int index = 0;
			for (StorageView<FluidVariant> view : storage.iterable(t)) {
				if (index == tank) {
					return view.getCapacity();
				}
				index++;
			}
			t.abort();
		}
		return 0;
	}

	@Override
	public long fill(FluidStack stack, boolean sim) {
		if (stack.isEmpty())
			return 0;
		try (Transaction t = Transaction.openOuter()) {
			long filled = storage.insert(stack.getType(), stack.getAmount(), t);
			if (!sim) {
				t.commit();
			}
			return filled;
		}
	}

	@Override
	public FluidStack drain(FluidStack stack, boolean sim) {
		if (stack.isEmpty())
			return FluidStack.EMPTY;
		try (Transaction t = Transaction.openOuter()) {
			long extracted = storage.extract(stack.getType(), stack.getAmount(), t);
			if (!sim) {
				t.commit();
			}
			return stack.copy().setAmount(extracted);
		}
	}

	@Override
	public FluidStack drain(long amount, boolean sim) {
		FluidStack extracted = FluidStack.EMPTY;
		if (amount == 0)
			return extracted;
		long toExtract = amount;
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<FluidVariant> view : storage.iterable(t)) {
				FluidVariant var = view.getResource();
				if (var.isBlank() || !extracted.canFill(var)) continue;
				long drained = view.extract(var, toExtract, t);
				toExtract -= drained;
				if (drained != 0) {
					if (extracted.isEmpty()) {
						extracted = new FluidStack(var, drained);
					} else if (extracted.canFill(var)) {
						extracted.grow(drained);
					}
				}
				if (toExtract == 0) break;
			}
			if (!sim)
				t.commit();
		}
		return extracted;
	}
}
