package com.simibubi.create.api.fluids.transfer;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.Unmodifiable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;

import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.fluids.FluidStack;

/**
 * Interface defining generic emptying behaviour for container-like items
 * that needs special handling or to be populated on runtime (e.g. Potions).
 * <p>
 * For plain cases, create and use {@link FillingRecipe} instead.
 * @see GenericItemEmptying
 */
public interface ItemEmptying {
	SimpleRegistry.Multi<Item, ItemEmptying> REGISTRY = SimpleRegistry.Multi.create();

	/**
	 * Checks if the item can be emptied.
	 * @implNote The item check is already guarded by registry,
	 * overrides only needs to perform stack-sensitive checks.
	 * @return If the item can be emptied.
	 */
	default boolean canItemBeEmptied(Level world, ItemStack stack) {
		return true;
	}

	/**
	 * Empty the item and return the result fluid and container.
	 * @return The result fluid and container.
	 * @implNote Unlike {@link ItemFilling#fillItem(Level, ItemStack, FluidStack)},
	 * overrides should handle the consumption of the item on their own and respect the {@code simulate} parameter.
	 */
	Pair<FluidStack, ItemStack> emptyItem(Level world, ItemStack stack, boolean simulate);

	/**
	 * Provide the {@link EmptyingRecipe recipe} representation of the filling behaviour for recipe viewers like JEI.
	 * @param consumer the consumer of the recipes
	 * @param itemIngredients the known item ingredients for providing recipes
	 * @param fluidIngredients the known fluid ingredients for providing recipes
	 */
	default void provideRecipes(Consumer<EmptyingRecipe> consumer, @Unmodifiable Collection<ItemStack> itemIngredients, @Unmodifiable Collection<FluidStack> fluidIngredients) {}
}
