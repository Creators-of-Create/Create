package com.simibubi.create.content.contraptions.fluids;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CombinedFluidHandler implements IFluidHandler {
	private final int capacity;
	private final FluidStack[] tanks;

	public CombinedFluidHandler(int tankNumber, int capacity) {
		this.capacity = capacity;
		this.tanks = new FluidStack[tankNumber];
		Arrays.fill(tanks, FluidStack.EMPTY);
	}

	@Override
	public int getTanks() {
		return tanks.length;
	}

	@Nonnull
	@Override
	public FluidStack getFluidInTank(int tank) {
		if (tank < 0 || tank >= tanks.length)
			return FluidStack.EMPTY;
		return tanks[tank];
	}

	@Override
	public int getTankCapacity(int tank) {
		return capacity;
	}

	@Override
	public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
		return (!stack.isEmpty()) && (tanks[tank].isEmpty() || tanks[tank].isFluidEqual(stack))
			&& tanks[tank].getAmount() < capacity;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		int tankIndex;
		int amount = resource.getAmount();
		while ((tankIndex = getFittingFluidSlot(resource)) != -1) {
			int newAmount = MathHelper.clamp(amount - capacity - tanks[tankIndex].getAmount(), 0, Integer.MAX_VALUE);
			if (action == FluidAction.EXECUTE)
				if (tanks[tankIndex].isEmpty())
					tanks[tankIndex] = new FluidStack(resource.getFluid(), amount - newAmount);
				else
					tanks[tankIndex].grow(amount - newAmount);
			amount = newAmount;
			if (amount == 0)
				return 0;
		}
		return amount;
	}

	@Nonnull
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;

		FluidStack stack = new FluidStack(resource, 0);

		for (int i = 0; i < tanks.length; i++) {
			if (tanks[i].isFluidEqual(resource)) {
				stack.grow(tanks[i].getAmount());
				if (action == FluidAction.EXECUTE)
					tanks[i] = FluidStack.EMPTY;
			}
		}

		return stack.isEmpty() ? FluidStack.EMPTY : stack;
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		FluidStack stack = new FluidStack(tanks[0].getFluid(), 0);

		for (int i = 0; i < tanks.length; i++) {
			if (stack.isEmpty() || tanks[i].isFluidEqual(stack)) {
				int newDrainAmount = MathHelper.clamp(stack.getAmount() + tanks[i].getAmount(), 0, maxDrain);
				if (action == FluidAction.EXECUTE) {
					tanks[i].shrink(newDrainAmount - stack.getAmount());
					if (tanks[i].isEmpty())
						tanks[i] = FluidStack.EMPTY;
				}
				if (stack.isEmpty()) 
					stack = tanks[i].copy();
				if (stack.isEmpty())
					continue;
				stack.setAmount(newDrainAmount);
			}
		}

		return stack.isEmpty() ? FluidStack.EMPTY : stack;
	}

	private int getFittingFluidSlot(FluidStack fluidStack) {
		return IntStream.range(0, tanks.length)
			.filter(i -> isFluidValid(i, fluidStack))
			.findFirst()
			.orElse(-1);
	}

	private void setFluid(FluidStack fluid, int tank) {
		tanks[tank] = fluid;
	}

	public CombinedFluidHandler readFromNBT(ListNBT fluidNBTs) {
		for (int i = 0; i < Math.min(tanks.length, fluidNBTs.size()); i++)
			setFluid(FluidStack.loadFluidStackFromNBT(fluidNBTs.getCompound(i)), i);
		return this;
	}

	public ListNBT getListNBT() {
		return Arrays.stream(tanks)
			.map(fluid -> fluid.writeToNBT(new CompoundNBT()))
			.collect(Collectors.toCollection(ListNBT::new));
	}

	public void forEachTank(NonNullConsumer<FluidStack> fluidStackConsumer) {
		Arrays.stream(tanks)
			.forEach(fluidStackConsumer::accept);
	}
}
