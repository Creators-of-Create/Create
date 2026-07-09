package com.simibubi.create.foundation.recipe;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.fluid.LazyComponentFluidIngredient;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllIngredients {
	public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Create.ID);
	public static final DeferredRegister<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, Create.ID);

	// Unused currently
	public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<LazyComponentFluidIngredient>> COMPONENT_FLUID_INGREDIENT =
		FLUID_INGREDIENT_TYPES.register("components", () -> new FluidIngredientType<>(LazyComponentFluidIngredient.CODEC));

	@Internal
	public static void register(IEventBus modEventBus) {
		INGREDIENT_TYPES.register(modEventBus);
		FLUID_INGREDIENT_TYPES.register(modEventBus);
	}
}
