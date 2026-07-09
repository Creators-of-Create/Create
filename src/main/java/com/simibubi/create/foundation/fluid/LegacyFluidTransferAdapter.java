package com.simibubi.create.foundation.fluid;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
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
		if (handler instanceof HosePulleyFluidHandler)
			return insertHose(resource, amount, transaction);
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
		if (handler instanceof HosePulleyFluidHandler)
			return extractHose(resource, amount, transaction);
		FluidStack stack = resource.toStack(amount);
		FluidStack extracted = handler.drain(stack, FluidAction.SIMULATE);
		if (extracted.isEmpty())
			return 0;
		updateSnapshots(transaction);
		return handler.drain(extracted, FluidAction.EXECUTE)
			.getAmount();
	}

	private int insertHose(FluidResource resource, int amount, TransactionContext transaction) {
		FluidStack stack = resource.toStack(amount);
		int inserted = handler.fill(stack, FluidAction.SIMULATE);
		if (inserted <= 0)
			return 0;
		FluidStack committed = stack.copyWithAmount(inserted);
		new RootCommitJournal(() -> handler.fill(committed.copy(), FluidAction.EXECUTE)).updateSnapshots(transaction);
		return inserted;
	}

	private int extractHose(FluidResource resource, int amount, TransactionContext transaction) {
		FluidStack stack = resource.toStack(amount);
		FluidStack extracted = handler.drain(stack, FluidAction.SIMULATE);
		if (extracted.isEmpty())
			return 0;
		FluidStack committed = extracted.copy();
		new RootCommitJournal(() -> handler.drain(committed.copy(), FluidAction.EXECUTE)).updateSnapshots(transaction);
		return extracted.getAmount();
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
		else if (handler instanceof HosePulleyFluidHandler hose && snapshot.size() == 1)
			hose.getInternalTank()
				.setFluid(snapshot.get(0).copy());
		else if (handler instanceof CombinedTankWrapper combined)
			restoreCombinedTank(combined, snapshot);
		else
			restoreGenericHandler(handler, snapshot);
	}

	private static void restoreCombinedTank(CombinedTankWrapper combined, List<FluidStack> snapshot) {
		for (int slot = 0; slot < snapshot.size() && slot < combined.getTanks(); slot++)
			restoreTankSlot(combined, slot, snapshot.get(slot));
	}

	private static void restoreTankSlot(IFluidHandler handler, int slot, FluidStack snapshot) {
		if (handler instanceof FluidTank tank && slot == 0) {
			tank.setFluid(snapshot.copy());
			return;
		}
		if (!(handler instanceof CombinedTankWrapper combined))
			return;
		int index = combined.getIndexForSlot(slot);
		IFluidHandler nested = combined.getHandlerFromIndex(index);
		int nestedSlot = combined.getSlotFromIndex(slot, index);
		restoreTankSlot(nested, nestedSlot, snapshot);
	}

	private static void restoreGenericHandler(IFluidHandler handler, List<FluidStack> snapshot) {
		for (int slot = 0; slot < snapshot.size() && slot < handler.getTanks(); slot++) {
			FluidStack current = handler.getFluidInTank(slot);
			if (!current.isEmpty())
				handler.drain(current.copyWithAmount(current.getAmount()), FluidAction.EXECUTE);

			FluidStack restored = snapshot.get(slot);
			if (!restored.isEmpty())
				handler.fill(restored.copy(), FluidAction.EXECUTE);
		}
	}
}
