package com.simibubi.create.foundation.fluid;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class LegacyFluidTransferAdapter extends SnapshotJournal<List<FluidStack>> implements ResourceHandler<FluidResource> {
	private final IFluidHandler handler;

	public LegacyFluidTransferAdapter(IFluidHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getTanks();
	}

	@Override
	public FluidResource getResource(int index) {
		if (index < 0 || index >= handler.getTanks())
			return FluidResource.EMPTY;
		return FluidResource.of(handler.getFluidInTank(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		if (index < 0 || index >= handler.getTanks())
			return 0;
		return handler.getFluidInTank(index)
			.getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		if (index < 0 || index >= handler.getTanks())
			return 0;
		return handler.getTankCapacity(index);
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		if (index < 0 || index >= handler.getTanks() || resource.isEmpty())
			return false;
		return handler.isFluidValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (!isValid(index, resource) || amount <= 0)
			return 0;
		FluidStack stack = resource.toStack(amount);
		int inserted = handler.fill(stack, FluidAction.SIMULATE);
		if (inserted <= 0)
			return 0;
		updateSnapshots(transaction);
		return handler.fill(stack.copyWithAmount(inserted), FluidAction.EXECUTE);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (index < 0 || index >= handler.getTanks() || resource.isEmpty() || amount <= 0)
			return 0;
		FluidStack stack = resource.toStack(amount);
		FluidStack extracted = handler.drain(stack, FluidAction.SIMULATE);
		if (extracted.isEmpty())
			return 0;
		updateSnapshots(transaction);
		return handler.drain(extracted, FluidAction.EXECUTE)
			.getAmount();
	}

	@Override
	protected List<FluidStack> createSnapshot() {
		List<FluidStack> snapshot = new ArrayList<>(handler.getTanks());
		for (int i = 0; i < handler.getTanks(); i++)
			snapshot.add(handler.getFluidInTank(i).copy());
		return snapshot;
	}

	@Override
	protected void revertToSnapshot(List<FluidStack> snapshot) {
		if (handler instanceof FluidTank tank && snapshot.size() == 1)
			tank.setFluid(snapshot.get(0).copy());
	}
}
