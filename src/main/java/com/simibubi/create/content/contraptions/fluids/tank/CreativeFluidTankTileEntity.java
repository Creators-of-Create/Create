package com.simibubi.create.content.contraptions.fluids.tank;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class CreativeFluidTankTileEntity extends FluidTankTileEntity {

	public CreativeFluidTankTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	protected SmartFluidTank createInventory() {
		return new CreativeSmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
	}
	
	@Override
	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	public static class CreativeSmartFluidTank extends SmartFluidTank {

		public CreativeSmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
			super(capacity, updateCallback);
		}
		
		@Override
		public int getFluidAmount() {
			return getFluid().isEmpty() ? 0 : getTankCapacity(0);
		}
		
		public void setContainedFluid(FluidStack fluidStack) {
			fluid = fluidStack.copy();
			if (!fluidStack.isEmpty()) 
				fluid.setAmount(getTankCapacity(0));
			onContentsChanged();
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			return resource.getAmount();
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return super.drain(resource, FluidAction.SIMULATE);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return super.drain(maxDrain, FluidAction.SIMULATE);
		}

	}

}
