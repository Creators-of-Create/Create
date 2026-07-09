package com.simibubi.create.foundation.fluid;

import java.util.List;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class LegacyFluidIngredientBridge {
	public static List<FluidStack> getFluids(SizedFluidIngredient ingredient) {
		if (ingredient.ingredient() instanceof LazyComponentFluidIngredient lazy)
			return lazy.getStacks(ingredient.amount());
		return ingredient.ingredient()
			.fluids()
			.stream()
			.map(fluid -> new FluidStack(fluid, ingredient.amount()))
			.toList();
	}

	public static FluidStack[] getFluidsArray(SizedFluidIngredient ingredient) {
		return getFluids(ingredient).toArray(FluidStack[]::new);
	}
}
