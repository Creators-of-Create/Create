package com.simibubi.create.content.trains.schedule.condition;

import com.simibubi.create.content.logistics.filter.FilterItemStack;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidThresholdConditionClient {

	public static FluidStack loadFluid(FilterItemStack stack) {
		return stack.fluid(Minecraft.getInstance().level);
	}

}
