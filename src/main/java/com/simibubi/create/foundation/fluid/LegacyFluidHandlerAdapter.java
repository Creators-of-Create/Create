package com.simibubi.create.foundation.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class LegacyFluidHandlerAdapter implements IFluidHandler {
	private final ResourceHandler<FluidResource> handler;

	public LegacyFluidHandlerAdapter(ResourceHandler<FluidResource> handler) {
		this.handler = handler;
	}

	public static IFluidHandler of(ResourceHandler<FluidResource> handler) {
		return handler == null ? null : new LegacyFluidHandlerAdapter(handler);
	}

	@Override
	public int getTanks() {
		return handler.size();
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		FluidResource resource = handler.getResource(tank);
		return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(handler.getAmountAsInt(tank));
	}

	@Override
	public int getTankCapacity(int tank) {
		return handler.getCapacityAsInt(tank, FluidResource.EMPTY);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return !stack.isEmpty() && handler.isValid(tank, FluidResource.of(stack));
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return 0;
		try (Transaction transaction = Transaction.openRoot()) {
			int inserted = handler.insert(FluidResource.of(resource), resource.getAmount(), transaction);
			if (action.execute())
				transaction.commit();
			return inserted;
		}
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		try (Transaction transaction = Transaction.openRoot()) {
			int extracted = handler.extract(FluidResource.of(resource), resource.getAmount(), transaction);
			if (action.execute())
				transaction.commit();
			return extracted == 0 ? FluidStack.EMPTY : resource.copyWithAmount(extracted);
		}
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (maxDrain <= 0)
			return FluidStack.EMPTY;
		try (Transaction transaction = Transaction.openRoot()) {
			var extracted = ResourceHandlerUtil.extractFirst(handler, resource -> true, maxDrain, transaction);
			if (action.execute())
				transaction.commit();
			return extracted == null ? FluidStack.EMPTY : extracted.resource().toStack(extracted.amount());
		}
	}
}
