package com.simibubi.create.foundation.fluid;

import java.util.function.Consumer;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import javax.annotation.Nullable;

public class SmartFluidTank extends FluidTank {

	private Consumer<FluidStack> updateCallback;

	public SmartFluidTank(long capacity, Consumer<FluidStack> updateCallback) {
		super(capacity);
		this.updateCallback = updateCallback;
	}

	@Override
	protected void onContentsChanged() {
		super.onContentsChanged();
		updateCallback.accept(getFluid());
	}

	@Override
	public void setFluid(FluidStack fluid) {
		setFluid(fluid, null);
	}

	public void setFluid(FluidStack stack, @Nullable TransactionContext ctx) {
		if (ctx != null) updateSnapshots(ctx);
		super.setFluid(stack);
		if (ctx == null) updateCallback.accept(stack);
		else TransactionCallback.onSuccess(ctx, () -> updateCallback.accept(stack));
	}
}
