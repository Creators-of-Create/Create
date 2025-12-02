package com.simibubi.create.api.behaviour.spouting;

import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public enum OpenTankSpoutingBehavior implements BlockSpoutingBehaviour {
	INSTANCE;

	@Override
	public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
		ICapabilityProvider be = level.getBlockEntity(pos);
		if(be == null)
			return 0;

		LazyOptional<IFluidHandler> lazyCapability = be.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP);
		if(!lazyCapability.isPresent())
			return 0;

		IFluidHandler tank = lazyCapability.orElseThrow(AssertionError::new);

		if(simulate)
			return tank.fill(availableFluid, FluidAction.SIMULATE);
		return tank.fill(availableFluid, FluidAction.EXECUTE);
	}
}
