package com.simibubi.create.foundation.data.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;

/**
 * Used to represent fluid outputs in recipe datagen without needing the fluid to exist at runtime.
 */
@ApiStatus.Internal
public final class DatagenFluidStack extends FluidStack {
	private final ResourceLocation actualFluid;

	public DatagenFluidStack(ResourceLocation fluid, int amount) {
		// This fluid is a farce
		super(Fluids.WATER, amount);
		actualFluid = fluid;
	}

	/**
	 * Supersedes the result of getFluid() for the purpose of obtaining a string representation of the fluid
	 * @return String value of the actual fluid's ResourceLocation
	 */
	public String getActualFluid(){
		return actualFluid.toString();
	}
}
