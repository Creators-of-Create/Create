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
		return FluidStack.empty();
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
		long finalVal = stack.getAmount();
		if(stack.getType().isBlank())
			return finalVal;
		try (Transaction t = Transaction.openOuter()) {
			long remainder = storage.insert(stack.getType(), stack.getAmount(), t);
			if (remainder != 0) {
				finalVal = remainder;
			}
			if (!sim) {
				t.commit();
			}
		}
		return finalVal;
	}

	@Override
	public FluidStack drain(FluidStack stack, boolean sim) {
		FluidStack finalVal;
		try (Transaction t = Transaction.openOuter()) {
			int index = 0;
			long extracted = storage.extract(stack.getType(), stack.getAmount(), t);
			finalVal = new FluidStack(stack.getType(), extracted);
			if (!sim) {
				t.commit();
			}
		}
		return finalVal;
	}

	@Override
	public FluidStack drain(long amount, boolean sim) {
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<FluidVariant> view : storage.iterable(t)) {
				FluidVariant var = view.getResource();
				if (var.isBlank()) continue;
				long extracted = view.extract(var, amount, t);
				if (extracted != 0) {
					if (!sim) t.commit();
					return new FluidStack(var, extracted);
				}
			}
		}
		return FluidStack.empty();
	}
}
