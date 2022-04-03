package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.function.Supplier;

import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;

public class HosePulleyFluidHandler implements SingleSlotStorage<FluidVariant> {

	// The dynamic interface

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (!internalTank.isEmpty() && !internalTank.getFluid().canFill(resource))
			return 0;
		if (resource.isBlank() || !FluidHelper.hasBlockState(resource.getFluid()))
			return 0;

		long inserted = internalTank.insert(resource, maxAmount, transaction);
		if (internalTank.amount >= FluidConstants.BUCKET && predicate.get()) {
			if (filler.tryDeposit(resource.getFluid(), rootPosGetter.get(), transaction)) {
				drainer.counterpartActed(transaction);
				internalTank.extract(resource, FluidConstants.BUCKET, transaction);
			}
		}
		return inserted;
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (resource != null && !internalTank.isEmpty() && !internalTank.getFluid().canFill(resource))
			return 0;
		if (internalTank.getFluidAmount() >= FluidConstants.BUCKET)
			return internalTank.extract(resource, maxAmount, transaction);
		BlockPos pos = rootPosGetter.get();
		FluidStack returned = drainer.getDrainableFluid(pos);
		if (!predicate.get() || !drainer.pullNext(pos, transaction))
			return internalTank.extract(resource, maxAmount, transaction);

		filler.counterpartActed(transaction);
		FluidStack leftover = returned.copy();
		long available = FluidConstants.BUCKET + internalTank.getFluidAmount();
		long drained;

		if (!internalTank.isEmpty() && !internalTank.getFluid()
				.isFluidEqual(returned) || returned.isEmpty())
			return internalTank.extract(resource, maxAmount, transaction);

		if (resource != null && !returned.canFill(resource))
			return 0;

		drained = Math.min(maxAmount, available);
		returned.setAmount(drained);
		leftover.setAmount(available - drained);
		if (!leftover.isEmpty())
			internalTank.setFluid(leftover, transaction);
		return returned.getAmount();
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public FluidVariant getResource() {
		if (!internalTank.isResourceBlank() || drainer.tileEntity.getLevel() == null) return internalTank.getResource();
		FluidState state = drainer.tileEntity.getLevel().getFluidState(rootPosGetter.get());
		return FluidVariant.of(state.getType());
	}

	@Override
	public long getAmount() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return Long.MAX_VALUE;
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
}
