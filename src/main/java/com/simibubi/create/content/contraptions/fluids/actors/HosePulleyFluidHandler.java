package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;

public class HosePulleyFluidHandler implements IFluidHandler {

	// The dynamic interface

	@Override
	public long fill(FluidStack resource, boolean sim) {
		if (!internalTank.isEmpty() && !resource.isFluidEqual(internalTank.getFluid()))
			return 0;
		if (resource.isEmpty() || !FluidHelper.hasBlockState(resource.getFluid()))
			return 0;

		long diff = resource.getAmount();
		long totalAmountAfterFill = diff + internalTank.getFluidAmount();
		FluidStack remaining = resource.copy();

		if (predicate.get() && totalAmountAfterFill >= FluidConstants.BUCKET) {
			if (filler.tryDeposit(resource.getFluid(), rootPosGetter.get(), sim)) {
				drainer.counterpartActed();
				remaining.shrink(FluidConstants.BUCKET);
				diff -= FluidConstants.BUCKET;
			}
		}

		if (sim)
			return diff <= 0 ? resource.getAmount() : internalTank.fill(remaining, sim);
		if (diff <= 0) {
			internalTank.drain(-diff, false);
			return resource.getAmount();
		}

		return internalTank.fill(remaining, sim);
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		if (internalTank.isEmpty())
			return drainer.getDrainableFluid(rootPosGetter.get());
		return internalTank.getFluidInTank(tank);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean sim) {
		return drainInternal(resource.getAmount(), resource, sim);
	}

	@Override
	public FluidStack drain(long maxDrain, boolean sim) {
		return drainInternal(maxDrain, null, sim);
	}

	private FluidStack drainInternal(long maxDrain, @Nullable FluidStack resource, boolean sim) {
		if (resource != null && !internalTank.isEmpty() && !resource.isFluidEqual(internalTank.getFluid()))
			return FluidStack.EMPTY;
		if (internalTank.getFluidAmount() >= FluidConstants.BUCKET)
			return internalTank.drain(maxDrain, sim);
		BlockPos pos = rootPosGetter.get();
		FluidStack returned = drainer.getDrainableFluid(pos);
		if (!predicate.get() || !drainer.pullNext(pos, sim))
			return internalTank.drain(maxDrain, sim);

		filler.counterpartActed();
		FluidStack leftover = returned.copy();
		long available = FluidConstants.BUCKET + internalTank.getFluidAmount();
		long drained;

		if (!internalTank.isEmpty() && !internalTank.getFluid()
			.isFluidEqual(returned) || returned.isEmpty())
			return internalTank.drain(maxDrain, sim);

		if (resource != null && !returned.isFluidEqual(resource))
			return FluidStack.EMPTY;

		drained = Math.min(maxDrain, available);
		returned.setAmount(drained);
		leftover.setAmount(available - drained);
		if (!sim && !leftover.isEmpty())
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
	public long getTankCapacity(int tank) {
		return internalTank.getTankCapacity(tank);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return internalTank.isFluidValid(tank, stack);
	}

}
