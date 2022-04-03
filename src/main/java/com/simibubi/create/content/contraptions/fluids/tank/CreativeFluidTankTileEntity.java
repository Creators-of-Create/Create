package com.simibubi.create.content.contraptions.fluids.tank;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
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
			return getFluid().isEmpty() ? 0 : getCapacity();
		}

		public void setContainedFluid(FluidStack fluidStack) {
			FluidStack fluid = fluidStack.copy();
			if (!fluidStack.isEmpty())
				fluid.setAmount(getCapacity(fluid.getType()));
			setFluid(fluid);
			onContentsChanged();
		}

		@Override
		public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
			return maxAmount;
		}

		@Override
		public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
			return maxAmount;
		}
	}

}
