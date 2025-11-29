package com.simibubi.create.content.processing.basin;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;

import com.simibubi.create.content.processing.basin.BasinMountedFluidStorage.MountedBasinTankHalf;

import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

public class BasinMountedFluidStorageType extends MountedFluidStorageType<BasinMountedFluidStorage> {
	public BasinMountedFluidStorageType() { super(BasinMountedFluidStorage.CODEC); }

	@Override
	public @Nullable BasinMountedFluidStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if(be instanceof BasinBlockEntity basin) {
			TankSegment[] inputTanks = basin.inputTank.getTanks();
			FluidStack firstInputStack = inputTanks[0].getTank().getFluid();
			FluidStack secondInputStack = inputTanks[1].getTank().getFluid();
			MountedBasinTankHalf inputTankHalf = MountedBasinTankHalf.fromStacks(
				true,
				firstInputStack,
				secondInputStack,
				firstInputStack.getAmount(),
				secondInputStack.getAmount()
			);

			TankSegment[] outputTanks = basin.outputTank.getTanks();
			FluidStack firstOutputStack = outputTanks[0].getTank().getFluid();
			FluidStack secondOutputStack = outputTanks[1].getTank().getFluid();
			MountedBasinTankHalf outputTankHalf = MountedBasinTankHalf.fromStacks(
				false,
				firstOutputStack,
				secondOutputStack,
				firstOutputStack.getAmount(),
				secondOutputStack.getAmount()
			);

			// i'm just... assuming these input and output tanks have two segments........ since that's how it's hardcoded

			return new BasinMountedFluidStorage(inputTankHalf, outputTankHalf);
		}

		return null;
	}
}
