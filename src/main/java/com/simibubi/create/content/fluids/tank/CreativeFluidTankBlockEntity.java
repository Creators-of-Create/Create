package com.simibubi.create.content.fluids.tank;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateCodecs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CreativeFluidTankBlockEntity extends FluidTankBlockEntity {

	public CreativeFluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
		public static final Codec<CreativeSmartFluidTank> CODEC = RecordCodecBuilder.create(i -> i.group(
			CreateCodecs.FLUID_STACK_CODEC.fieldOf("fluid").forGetter(FluidTank::getFluid),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(FluidTank::getCapacity)
		).apply(i, (fluid, capacity) -> {
			CreativeSmartFluidTank tank = new CreativeSmartFluidTank(capacity, $ -> {
			});
			tank.setFluid(fluid);
			return tank;
		}));

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
