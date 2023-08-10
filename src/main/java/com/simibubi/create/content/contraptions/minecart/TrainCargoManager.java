package com.simibubi.create.content.contraptions.minecart;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.content.contraptions.Contraption.ContraptionInvWrapper;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TrainCargoManager extends MountedStorageManager {

	int ticksSinceLastExchange;
	AtomicInteger version;

	public TrainCargoManager() {
		version = new AtomicInteger();
		ticksSinceLastExchange = 0;
	}

	@Override
	public void createHandlers() {
		super.createHandlers();
	}

	@Override
	protected ContraptionInvWrapper wrapItems(Collection<IItemHandlerModifiable> list, boolean fuel) {
		if (fuel)
			return super.wrapItems(list, fuel);
		return new CargoInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));
	}

	@Override
	protected CombinedTankWrapper wrapFluids(Collection<IFluidHandler> list) {
		return new CargoTankWrapper(Arrays.copyOf(list.toArray(), list.size(), IFluidHandler[].class));
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		nbt.putInt("TicksSinceLastExchange", ticksSinceLastExchange);
	}

	@Override
	public void read(CompoundTag nbt, Map<BlockPos, BlockEntity> presentBlockEntities, boolean clientPacket) {
		super.read(nbt, presentBlockEntities, clientPacket);
		ticksSinceLastExchange = nbt.getInt("TicksSinceLastExchange");
	}

	public void resetIdleCargoTracker() {
		ticksSinceLastExchange = 0;
	}
	
	public void tickIdleCargoTracker() {
		ticksSinceLastExchange++;
	}
	
	public int getTicksSinceLastExchange() {
		return ticksSinceLastExchange;
	}
	
	public int getVersion() {
		return version.get();
	}
	
	void changeDetected() {
		version.incrementAndGet();
		resetIdleCargoTracker();
	}

	class CargoInvWrapper extends ContraptionInvWrapper {

		public CargoInvWrapper(IItemHandlerModifiable... itemHandler) {
			super(false, itemHandler);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			ItemStack remainder = super.insertItem(slot, stack, simulate);
			if (!simulate && stack.getCount() != remainder.getCount())
				changeDetected();
			return remainder;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack extracted = super.extractItem(slot, amount, simulate);
			if (!simulate && !extracted.isEmpty())
				changeDetected();
			return extracted;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (!stack.equals(getStackInSlot(slot)))
				changeDetected();
			super.setStackInSlot(slot, stack);
		}

	}

	class CargoTankWrapper extends CombinedTankWrapper {

		public CargoTankWrapper(IFluidHandler... fluidHandler) {
			super(fluidHandler);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			int filled = super.fill(resource, action);
			if (action.execute() && filled > 0)
				changeDetected();
			return filled;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			FluidStack drained = super.drain(resource, action);
			if (action.execute() && !drained.isEmpty())
				changeDetected();
			return drained;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			FluidStack drained = super.drain(maxDrain, action);
			if (action.execute() && !drained.isEmpty())
				changeDetected();
			return drained;
		}
		
	}

}
