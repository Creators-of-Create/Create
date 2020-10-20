package com.simibubi.create.content.contraptions.fluids.potion;

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
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionMixingRecipeManager {

	public static Map<Item, List<MixingRecipe>> ALL = new HashMap<>();

	public static List<MixingRecipe> getAllBrewingRecipes() {
		List<MixingRecipe> mixingRecipes = new ArrayList<>();

		// Vanilla
		for (IBrewingRecipe iBrewingRecipe : BrewingRecipeRegistry.getRecipes()) {
			if (!(iBrewingRecipe instanceof VanillaBrewingRecipe))
				continue;

			List<ItemStack> bottles = new ArrayList<>();
			PotionBrewing.POTION_ITEMS.forEach(i -> {
				for (ItemStack itemStack : i.getMatchingStacks())
					bottles.add(itemStack);
			});

			Collection<ItemStack> reagents = getAllReagents(iBrewingRecipe);

			Set<ItemStack> basicPotions = new HashSet<>();
			for (Potion potion : ForgeRegistries.POTION_TYPES.getValues()) {
				if (potion == Potions.EMPTY)
					continue;
				for (ItemStack stack : bottles)
					basicPotions.add(PotionUtils.addPotionToItemStack(stack.copy(), potion));
			}

			Set<String> uniqueKeys = new HashSet<>();
			List<ItemStack> potionFrontier = new ArrayList<>();
			List<ItemStack> newPotions = new ArrayList<>();
			potionFrontier.addAll(basicPotions);

			int recipeIndex = 0;

			while (!potionFrontier.isEmpty()) {
				newPotions.clear();

				for (ItemStack inputPotionStack : potionFrontier) {
					Potion inputPotion = PotionUtils.getPotionFromItem(inputPotionStack);

					for (ItemStack potionReagent : reagents) {
						ItemStack outputPotionStack = iBrewingRecipe.getOutput(inputPotionStack.copy(), potionReagent);
						if (outputPotionStack.isEmpty())
							continue;

						String uniqueKey = potionReagent.getItem()
							.getRegistryName()
							.toString() + "_"
							+ inputPotion.getRegistryName()
								.toString()
							+ "_" + inputPotionStack.getItem()
								.getRegistryName()
								.toString();

						if (!uniqueKeys.add(uniqueKey))
							continue;

						if (inputPotionStack.getItem() == outputPotionStack.getItem()) {
							Potion outputPotion = PotionUtils.getPotionFromItem(outputPotionStack);
							if (outputPotion == Potions.WATER)
								continue;
						}

						FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(inputPotionStack);
						FluidStack fluidFromPotionItem2 = PotionFluidHandler.getFluidFromPotionItem(outputPotionStack);
						fluidFromPotionItem.setAmount(1000);
						fluidFromPotionItem2.setAmount(1000);

						MixingRecipe mixingRecipe = new ProcessingRecipeBuilder<>(MixingRecipe::new,
							Create.asResource("potion_" + recipeIndex++)).require(Ingredient.fromStacks(potionReagent))
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

			break;
		}

		// TODO Modded brewing recipes?

		return mixingRecipes;
	}

	public static Collection<ItemStack> getAllReagents(IBrewingRecipe recipe) {
		return ForgeRegistries.ITEMS.getValues()
			.stream()
			.map(ItemStack::new)
			.filter(recipe::isIngredient)
			.collect(Collectors.toList());
	}

	public static final ReloadListener<Object> LISTENER = new ReloadListener<Object>() {

		@Override
		protected Object prepare(IResourceManager p_212854_1_, IProfiler p_212854_2_) {
			return new Object();
		}

		@Override
		protected void apply(Object p_212853_1_, IResourceManager p_212853_2_, IProfiler p_212853_3_) {
			ALL.clear();
			getAllBrewingRecipes().forEach(recipe -> {
				for (Ingredient ingredient : recipe.getIngredients()) {
					for (ItemStack itemStack : ingredient.getMatchingStacks()) {
						ALL.computeIfAbsent(itemStack.getItem(), t -> new ArrayList<>())
							.add(recipe);
						return;
					}
				}
			});
		}

	};

}
