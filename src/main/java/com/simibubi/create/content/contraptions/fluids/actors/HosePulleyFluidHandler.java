package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class HosePulleyFluidHandler implements IFluidHandler {

	// The dynamic interface

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (!internalTank.isEmpty() && !resource.isFluidEqual(internalTank.getFluid()))
			return 0;
		if (resource.isEmpty() || !FluidHelper.hasBlockState(resource.getFluid()))
			return 0;

		int diff = resource.getAmount();
		int totalAmountAfterFill = diff + internalTank.getFluidAmount();
		FluidStack remaining = resource.copy();
		boolean deposited = false;

		if (predicate.get() && totalAmountAfterFill >= 1000) {
			if (filler.tryDeposit(resource.getFluid(), rootPosGetter.get(), action.simulate())) {
				drainer.counterpartActed();
				remaining.shrink(1000);
				diff -= 1000;
				deposited = true;
			}
		}

		if (action.simulate())
			return diff <= 0 ? resource.getAmount() : internalTank.fill(remaining, action);
		if (diff <= 0) {
			internalTank.drain(-diff, FluidAction.EXECUTE);
			return resource.getAmount();
		}

		return internalTank.fill(remaining, action) + (deposited ? 1000 : 0);
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		if (internalTank.isEmpty())
			return drainer.getDrainableFluid(rootPosGetter.get());
		return internalTank.getFluidInTank(tank);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return drainInternal(resource.getAmount(), resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return drainInternal(maxDrain, null, action);
	}

	private FluidStack drainInternal(int maxDrain, @Nullable FluidStack resource, FluidAction action) {
		if (resource != null && !internalTank.isEmpty() && !resource.isFluidEqual(internalTank.getFluid()))
			return FluidStack.EMPTY;
		if (internalTank.getFluidAmount() >= 1000)
			return internalTank.drain(maxDrain, action);
		BlockPos pos = rootPosGetter.get();
		FluidStack returned = drainer.getDrainableFluid(pos);
		if (!predicate.get() || !drainer.pullNext(pos, action.simulate()))
			return internalTank.drain(maxDrain, action);

		filler.counterpartActed();
		FluidStack leftover = returned.copy();
		int available = 1000 + internalTank.getFluidAmount();
		int drained;

		if (!internalTank.isEmpty() && !internalTank.getFluid()
			.isFluidEqual(returned) || returned.isEmpty())
			return internalTank.drain(maxDrain, action);

		if (resource != null && !returned.isFluidEqual(resource))
			return FluidStack.EMPTY;

		drained = Math.min(maxDrain, available);
		returned.setAmount(drained);
		leftover.setAmount(available - drained);
		if (action.execute() && !leftover.isEmpty())
			internalTank.setFluid(leftover);
		return returned;
	}

	//

	private SmartFluidTank internalTank;
	private FluidFillingBehaviour filler;
	private FluidDrainingBehaviour drainer;
	private Supplier<BlockPos> rootPosGetter;
	private Supplier<Boolean> predicate;

	public HosePulleyFluidHandler(SmartFluidTank internalTank, FluidFillingBehaviour filler,
		FluidDrainingBehaviour drainer, Supplier<BlockPos> rootPosGetter, Supplier<Boolean> predicate) {
		this.internalTank = internalTank;
		this.filler = filler;
		this.drainer = drainer;
		this.rootPosGetter = rootPosGetter;
		this.predicate = predicate;
	}

	@Override
	public int getTanks() {
		return internalTank.getTanks();
	}

	@Override
	public int getTankCapacity(int tank) {
		return internalTank.getTankCapacity(tank);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return internalTank.isFluidValid(tank, stack);
	}

	public SmartFluidTank getInternalTank() {
		return internalTank;
	}

}
