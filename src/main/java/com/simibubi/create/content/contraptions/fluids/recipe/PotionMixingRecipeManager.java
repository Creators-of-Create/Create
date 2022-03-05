package com.simibubi.create.content.contraptions.fluids.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;

import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.PotionBrewingAccessor;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

import static net.minecraft.world.item.alchemy.PotionBrewing.isIngredient;

public class PotionMixingRecipeManager {

	public static Map<Item, List<MixingRecipe>> ALL = new HashMap<>();

	public static List<MixingRecipe> getAllBrewingRecipes() {
		List<MixingRecipe> mixingRecipes = new ArrayList<>();

		// Vanilla

		List<ItemStack> bottles = new ArrayList<>();
		PotionBrewingAccessor.getALLOWED_CONTAINERS().forEach(i -> {
			for (ItemStack itemStack : i.getItems())
				bottles.add(itemStack);
		});

		Collection<ItemStack> reagents = getAllReagents();

		Set<ItemStack> basicPotions = new HashSet<>();
		for (Potion potion : Registry.POTION) {
			if (potion == Potions.EMPTY)
				continue;
			for (ItemStack stack : bottles)
				basicPotions.add(PotionUtils.setPotion(stack.copy(), potion));
		}

		Set<String> uniqueKeys = new HashSet<>();
		List<ItemStack> potionFrontier = new ArrayList<>();
		List<ItemStack> newPotions = new ArrayList<>();
		potionFrontier.addAll(basicPotions);

		int recipeIndex = 0;

		while (!potionFrontier.isEmpty()) {
			newPotions.clear();

			for (ItemStack inputPotionStack : potionFrontier) {
				Potion inputPotion = PotionUtils.getPotion(inputPotionStack);

				for (ItemStack potionReagent : reagents) {
					ItemStack outputPotionStack = getOutput(inputPotionStack.copy(), potionReagent);
					if (outputPotionStack.isEmpty())
						continue;

					String uniqueKey = Registry.ITEM.getKey(potionReagent.getItem())
						.toString() + "_"
						+ Registry.POTION.getKey(inputPotion)
							.toString()
						+ "_" + Registry.ITEM.getKey(inputPotionStack.getItem())
							.toString();

					if (!uniqueKeys.add(uniqueKey))
						continue;

					if (inputPotionStack.getItem() == outputPotionStack.getItem()) {
						Potion outputPotion = PotionUtils.getPotion(outputPotionStack);
							if (outputPotion == Potions.WATER)
								continue;
					}

					FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(inputPotionStack);
					FluidStack fluidFromPotionItem2 = PotionFluidHandler.getFluidFromPotionItem(outputPotionStack);
					fluidFromPotionItem.setAmount(FluidConstants.BUCKET);
					fluidFromPotionItem2.setAmount(FluidConstants.BUCKET);

					MixingRecipe mixingRecipe = new ProcessingRecipeBuilder<>(MixingRecipe::new,
						Create.asResource("potion_" + recipeIndex++)).require(Ingredient.of(potionReagent))
							.require(FluidIngredient.fromFluidStack(fluidFromPotionItem))
							.output(fluidFromPotionItem2)
							.requiresHeat(HeatCondition.HEATED)
							.build();

					mixingRecipes.add(mixingRecipe);
					newPotions.add(outputPotionStack);
					}
				}

			potionFrontier.clear();
			potionFrontier.addAll(newPotions);
		}

		// TODO Modded brewing recipes?

		return mixingRecipes;
	}

	public static ItemStack getOutput(ItemStack input, ItemStack ingredient)
	{
		if (!input.isEmpty() && !ingredient.isEmpty() && isIngredient(ingredient))
		{
			ItemStack result = PotionBrewing.mix(ingredient, input);
			if (result != input)
			{
				return result;
			}
			return ItemStack.EMPTY;
		}

		return ItemStack.EMPTY;
	}

	public static Collection<ItemStack> getAllReagents() {
		return Registry.ITEM
			.stream()
			.map(ItemStack::new)
			.filter(PotionBrewing::isIngredient)
			.collect(Collectors.toList());
	}

	public static final ResourceManagerReloadListener LISTENER = resourceManager -> {
		ALL.clear();
		getAllBrewingRecipes().forEach(recipe -> {
			for (Ingredient ingredient : recipe.getIngredients()) {
				for (ItemStack itemStack : ingredient.getItems()) {
					ALL.computeIfAbsent(itemStack.getItem(), t -> new ArrayList<>())
						.add(recipe);
					return;
				}
			}
		});
	};

}
