package com.simibubi.create.content.processing.basin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.fluid.LegacyFluidHandlerAdapter;
import com.simibubi.create.foundation.item.LegacyItemHandlerAdapter;
import com.simibubi.create.foundation.recipe.DummyCraftingContainer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;

public class BasinRecipe extends StandardProcessingRecipe<RecipeInput> {

	public static boolean match(BasinBlockEntity basin, Recipe<?> recipe) {
		FilteringBehaviour filter = basin.getFilter();
		if (filter == null)
			return false;

		boolean filterTest = filter.test(recipeResult(basin.getLevel(), recipe));
		if (recipe instanceof BasinRecipe basinRecipe) {
			if (basinRecipe.getRollableResults()
				.isEmpty()
				&& !basinRecipe.getFluidResults()
				.isEmpty())
				filterTest = filter.test(basinRecipe.getFluidResults()
					.get(0));
		}

		if (!filterTest)
			return false;

		return apply(basin, recipe, true);
	}

	public static boolean apply(BasinBlockEntity basin, Recipe<?> recipe) {
		return apply(basin, recipe, false);
	}

	private static boolean apply(BasinBlockEntity basin, Recipe<?> recipe, boolean test) {
		boolean isBasinRecipe = recipe instanceof BasinRecipe;
		IItemHandler availableItems = LegacyItemHandlerAdapter.of(
			basin.getLevel().getCapability(Capabilities.Item.BLOCK, basin.getBlockPos(), null));
		IFluidHandler availableFluids = LegacyFluidHandlerAdapter.of(
			basin.getLevel().getCapability(Capabilities.Fluid.BLOCK, basin.getBlockPos(), null));

		if (availableItems == null || availableFluids == null)
			return false;

		HeatLevel heat = basin.getHeatLevel();
		if (isBasinRecipe && !((BasinRecipe) recipe).getRequiredHeat()
			.testBlazeBurner(heat))
			return false;

		List<ItemStack> recipeOutputItems = new ArrayList<>();
		List<FluidStack> recipeOutputFluids = new ArrayList<>();

		List<Ingredient> ingredients = recipe instanceof ProcessingRecipe<?, ?> processingRecipe
			? new LinkedList<>(processingRecipe.getIngredients())
			: ingredientsFromPlacement(recipe.placementInfo());
		List<SizedFluidIngredient> fluidIngredients =
			isBasinRecipe ? ((BasinRecipe) recipe).getFluidIngredients() : Collections.emptyList();

		for (boolean simulate : Iterate.trueAndFalse) {

			if (!simulate && test)
				return true;

			int[] extractedItemsFromSlot = new int[availableItems.getSlots()];
			int[] extractedFluidsFromTank = new int[availableFluids.getTanks()];

			Ingredients:
			for (Ingredient ingredient : ingredients) {
				for (int slot = 0; slot < availableItems.getSlots(); slot++) {
					if (simulate && availableItems.getStackInSlot(slot)
						.getCount() <= extractedItemsFromSlot[slot])
						continue;
					ItemStack extracted = availableItems.extractItem(slot, 1, true);
					if (!ingredient.test(extracted))
						continue;
					if (!simulate)
						availableItems.extractItem(slot, 1, false);
					extractedItemsFromSlot[slot]++;
					continue Ingredients;
				}

				// something wasn't found
				return false;
			}

			boolean fluidsAffected = false;
			FluidIngredients:
			for (SizedFluidIngredient fluidIngredient : fluidIngredients) {
				int amountRequired = fluidIngredient.amount();

				for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
					FluidStack fluidStack = availableFluids.getFluidInTank(tank);
					if (simulate && fluidStack.getAmount() <= extractedFluidsFromTank[tank])
						continue;
					if (!fluidIngredient.test(fluidStack))
						continue;
					int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
					if (!simulate) {
						fluidStack.shrink(drainedAmount);
						fluidsAffected = true;
					}
					amountRequired -= drainedAmount;
					if (amountRequired != 0)
						continue;
					extractedFluidsFromTank[tank] += drainedAmount;
					continue FluidIngredients;
				}

				// something wasn't found
				return false;
			}

			if (fluidsAffected) {
				basin.getBehaviour(SmartFluidTankBehaviour.INPUT)
					.forEach(TankSegment::onFluidStackChanged);
				basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
					.forEach(TankSegment::onFluidStackChanged);
			}

			if (simulate) {
				CraftingInput remainderInput = new DummyCraftingContainer(availableItems, extractedItemsFromSlot)
					.asCraftInput();

				if (recipe instanceof BasinRecipe basinRecipe) {
					recipeOutputItems.addAll(basinRecipe.rollResults(basin.getLevel().getRandom()));

					for (FluidStack fluidStack : basinRecipe.getFluidResults())
						if (!fluidStack.isEmpty())
							recipeOutputFluids.add(fluidStack);
				} else {
					ItemStack result = recipeResult(basin.getLevel(), recipe);
					if (!result.isEmpty())
						recipeOutputItems.add(result);

					if (recipe instanceof CraftingRecipe craftingRecipe) {
						for (ItemStack stack : craftingRecipe.getRemainingItems(remainderInput))
							if (!stack.isEmpty())
								recipeOutputItems.add(stack);
					}
				}
			}

			if (!basin.acceptOutputs(recipeOutputItems, recipeOutputFluids, simulate)
				&& (!simulate || !recipeOutputItems.isEmpty()
					|| !basin.canAcceptInternalFluidOutputsAfterExtraction(recipeOutputFluids, extractedFluidsFromTank)))
				return false;
		}

		return true;
	}

	private static List<Ingredient> ingredientsFromPlacement(PlacementInfo placementInfo) {
		return placementInfo.isImpossibleToPlace()
			? new LinkedList<>()
			: new LinkedList<>(placementInfo.ingredients());
	}

	public static RecipeHolder<BasinRecipe> convertShapeless(RecipeHolder<?> recipe) {
		Recipe<?> value = recipe.value();
		BasinRecipe basinRecipe =
			new Builder<>(BasinRecipe::new, recipe.id().identifier())
				.withItemIngredients(value.placementInfo().ingredients().toArray(new Ingredient[0]))
				.withSingleItemOutput(recipeResult(value))
				.build();
		return new RecipeHolder<>(recipe.id(), basinRecipe);
	}

	private static ItemStack recipeResult(Recipe<?> recipe) {
		return recipe.display()
			.stream()
			.findFirst()
			.map(display -> display.result()
				.resolveForFirstStack(ContextMap.EMPTY))
			.orElse(ItemStack.EMPTY);
	}

	private static ItemStack recipeResult(Level level, Recipe<?> recipe) {
		if (recipe instanceof ProcessingRecipe<?, ?> processingRecipe)
			return processingRecipe.getResultItem(level.registryAccess());
		return recipe.display()
			.stream()
			.findFirst()
			.map(display -> display.result()
				.resolveForFirstStack(SlotDisplayContext.fromLevel(level)))
			.orElse(ItemStack.EMPTY);
	}

	protected BasinRecipe(IRecipeTypeInfo type, ProcessingRecipeParams params) {
		super(type, params);
	}

	public BasinRecipe(ProcessingRecipeParams params) {
		this(AllRecipeTypes.BASIN, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 64;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	@Override
	protected int getMaxFluidInputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 2;
	}

	@Override
	protected boolean canRequireHeat() {
		return true;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}

	@Override
	public boolean matches(RecipeInput input, @NotNull Level worldIn) {
		return false;
	}

}
