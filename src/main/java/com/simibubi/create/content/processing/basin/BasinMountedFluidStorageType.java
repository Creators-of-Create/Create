package com.simibubi.create.content.processing.basin;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;

import com.simibubi.create.content.processing.basin.BasinMountedFluidStorage.MountedBasinTankHalf;

import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class BasinMountedFluidStorageType extends MountedFluidStorageType<BasinMountedFluidStorage> {
	public BasinMountedFluidStorageType() { super(BasinMountedFluidStorage.CODEC); }

	@Override
	public @Nullable BasinMountedFluidStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if(be instanceof BasinBlockEntity basin) {
			TankSegment[] inputTanks = basin.inputTank.getTanks();
			MountedBasinTankHalf inputTankHalf = MountedBasinTankHalf.fromStacks(
				true,
				inputTanks[0].getTank().getFluid(),
				inputTanks[1].getTank().getFluid()
			);

			TankSegment[] outputTanks = basin.outputTank.getTanks();
			MountedBasinTankHalf outputTankHalf = MountedBasinTankHalf.fromStacks(
				false,
				outputTanks[0].getTank().getFluid(),
				outputTanks[1].getTank().getFluid()
			);

			// i'm just... assuming these input and output tanks have two segments........ since that's how it's hardcoded

			return new BasinMountedFluidStorage(inputTankHalf, outputTankHalf);
		}

		return null;
	}
}
