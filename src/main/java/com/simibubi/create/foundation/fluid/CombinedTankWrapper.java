package com.simibubi.create.foundation.fluid;

import net.createmod.catnip.data.Iterate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;

/**
 * Combines multiple IFluidHandlers into one interface (See CombinedInvWrapper
 * for items)
 */
public class CombinedTankWrapper implements IFluidHandler {

	protected final IFluidHandler[] itemHandler;
	protected final int[] baseIndex;
	protected final int tankCount;
	protected boolean enforceVariety;

	public CombinedTankWrapper(IFluidHandler... fluidHandlers) {
		this.itemHandler = fluidHandlers;
		this.baseIndex = new int[fluidHandlers.length];
		int index = 0;
		for (int i = 0; i < fluidHandlers.length; i++) {
			index += fluidHandlers[i].getTanks();
			baseIndex[i] = index;
		}
		this.tankCount = index;
	}

	public CombinedTankWrapper enforceVariety() {
		enforceVariety = true;
		return this;
	}

	@Override
	public int getTanks() {
		return tankCount;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		int index = getIndexForSlot(tank);
		IFluidHandler handler = getHandlerFromIndex(index);
		tank = getSlotFromIndex(tank, index);
		return handler.getFluidInTank(tank);
	}

	@Override
	public int getTankCapacity(int tank) {
		int index = getIndexForSlot(tank);
		IFluidHandler handler = getHandlerFromIndex(index);
		int localSlot = getSlotFromIndex(tank, index);
		return handler.getTankCapacity(localSlot);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		int index = getIndexForSlot(tank);
		IFluidHandler handler = getHandlerFromIndex(index);
		int localSlot = getSlotFromIndex(tank, index);
		return handler.isFluidValid(localSlot, stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return 0;

		int filled = 0;
		resource = resource.copy();

		boolean fittingHandlerFound = false;
		Outer: for (boolean searchPass : Iterate.trueAndFalse) {
			for (IFluidHandler iFluidHandler : itemHandler) {

				for (int i = 0; i < iFluidHandler.getTanks(); i++)
					if (searchPass && FluidStack.isSameFluidSameComponents(iFluidHandler.getFluidInTank(i), resource))
						fittingHandlerFound = true;

				if (searchPass && !fittingHandlerFound)
					continue;

				int filledIntoCurrent = iFluidHandler.fill(resource, action);
				resource.shrink(filledIntoCurrent);
				filled += filledIntoCurrent;

				if (resource.isEmpty())
					break Outer;
				if (fittingHandlerFound && (enforceVariety || filledIntoCurrent != 0))
					break Outer;
			}
		}

		return filled;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return resource;

		FluidStack drained = FluidStack.EMPTY;
		resource = resource.copy();

		for (IFluidHandler iFluidHandler : itemHandler) {
			FluidStack drainedFromCurrent = iFluidHandler.drain(resource, action);
			int amount = drainedFromCurrent.getAmount();
			resource.shrink(amount);

			if (!drainedFromCurrent.isEmpty() && (drained.isEmpty() || FluidStack.isSameFluidSameComponents(drainedFromCurrent, drained)))
				drained = new FluidStack(drainedFromCurrent.getFluidHolder(), amount + drained.getAmount(),
					drainedFromCurrent.getComponentsPatch());
			if (resource.isEmpty())
				break;
		}

		return drained;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		FluidStack drained = FluidStack.EMPTY;

		for (IFluidHandler iFluidHandler : itemHandler) {
			FluidStack drainedFromCurrent = iFluidHandler.drain(maxDrain, action);
			int amount = drainedFromCurrent.getAmount();
			maxDrain -= amount;

			if (!drainedFromCurrent.isEmpty() && (drained.isEmpty() || FluidStack.isSameFluidSameComponents(drainedFromCurrent, drained)))
				drained = new FluidStack(drainedFromCurrent.getFluidHolder(), amount + drained.getAmount(),
					drainedFromCurrent.getComponentsPatch());
			if (maxDrain == 0)
				break;
		}

		return drained;
	}

	protected int getIndexForSlot(int slot) {
		if (slot < 0)
			return -1;
		for (int i = 0; i < baseIndex.length; i++)
			if (slot - baseIndex[i] < 0)
				return i;
		return -1;
	}

	protected IFluidHandler getHandlerFromIndex(int index) {
		if (index < 0 || index >= itemHandler.length)
			return EmptyFluidHandler.INSTANCE;
		return itemHandler[index];
	}

	protected int getSlotFromIndex(int slot, int index) {
		if (index <= 0 || index >= baseIndex.length)
			return slot;
		return slot - baseIndex[index - 1];
	}
}
