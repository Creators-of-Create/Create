package com.simibubi.create.content.contraptions.fluids.tank;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeFluidTankTileEntity extends FluidTankTileEntity {

	public CreativeFluidTankTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected SmartFluidTank createInventory() {
		return new CreativeSmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	public static class CreativeSmartFluidTank extends SmartFluidTank {

		public CreativeSmartFluidTank(long capacity, Consumer<FluidStack> updateCallback) {
			super(capacity, updateCallback);
		}

		@Override
		public long getFluidAmount() {
			return getFluid().isEmpty() ? 0 : getTankCapacity(0);
		}

		public void setContainedFluid(FluidStack fluidStack) {
			fluid = fluidStack.copy();
			if (!fluidStack.isEmpty())
				fluid.setAmount(getTankCapacity(0));
			onContentsChanged();
		}

		@Override
		public long fill(FluidStack resource, boolean sim) {
			return resource.getAmount();
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean sim) {
			return super.drain(resource, true);
		}

		@Override
		public FluidStack drain(long maxDrain, boolean sim) {
			return super.drain(maxDrain, true);
		}

	}

}
