package com.simibubi.create.api.fluids.transfer;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.Unmodifiable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;

/**
 * Interface defining generic filling behaviour for container-like items
 * that does not provide {@link ForgeCapabilities#FLUID_HANDLER_ITEM}.
 * @see GenericItemFilling
 */
public interface ItemFilling {
	SimpleRegistry.Multi<Item, ItemFilling> REGISTRY = SimpleRegistry.Multi.create();

	/**
	 * Checks if the item can be filled, without any fluid context.
	 * @return If the item can be filled with certain fluids.
	 * @implNote The item check is already guarded by registry,
	 * overrides only needs to perform stack-sensitive checks.
	 */
	default boolean canItemBeFilled(Level world, ItemStack stack) {
		return true;
	}

	/**
	 * Returns the required amount of available fluid to fill the item.
	 * @return The required amount of available fluid to fill the item,
	 * or -1 if the item can't be filled with the provided fluid.
	 */
	int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid);

	/**
	 * Fills the item with required amount of available fluid.
	 * @return The filled item.
	 * @implNote The fluid comsumption is handled by the caller,
	 * overrides need not consume the fluid passed in.
	 */
	ItemStack fillItem(Level world, ItemStack stack, FluidStack availableFluid);

	/**
	 * Provide the {@link FillingRecipe recipe} representation of the filling behaviour for recipe viewers like JEI.
	 * @param consumer the consumer of the recipes
	 * @param itemIngredients the known item ingredients for providing recipes
	 * @param fluidIngredients the known fluid ingredients for providing recipes
	 */
	default void provideRecipes(Consumer<FillingRecipe> consumer, @Unmodifiable Collection<ItemStack> itemIngredients, @Unmodifiable Collection<FluidStack> fluidIngredients) {}

	/**
	 * Register a simple {@link ItemFilling} for an item.
	 * @param id the id of the recipe representation
	 * @param container the container item to be filled
	 * @param fluid the required fluid
	 * @param result the filled result
	 */
	static void registerSimple(ResourceLocation id, ItemLike container, FluidIngredient fluid, ItemStack result) {
		REGISTRY.add(container.asItem(), new Simple(id, container, fluid, result));
	}

	/**
	 * A simple {@link ItemFilling} implementation, basically equivalent to a normal {@link FillingRecipe}.
	 * @param id the id of the recipe representation
	 * @param container the container item to be filled
	 * @param fluid the required fluid
	 * @param result the filled result
	 */
	record Simple(ResourceLocation id, ItemLike container, FluidIngredient fluid, ItemStack result) implements ItemFilling {
		@Override
		public int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
			return fluid.test(availableFluid) ? fluid.getRequiredAmount() : -1;
		}

		@Override
		public ItemStack fillItem(Level world, ItemStack stack, FluidStack availableFluid) {
			return result;
		}

		@Override
		public void provideRecipes(Consumer<FillingRecipe> consumer, @Unmodifiable Collection<ItemStack> itemIngredients, @Unmodifiable Collection<FluidStack> fluidIngredients) {
			consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new, id)
				.require(fluid)
				.require(Ingredient.of(container))
				.output(result)
				.build()
			);
		}
	}
}
