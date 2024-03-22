package com.simibubi.create.content.fluids.potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionMixingRecipes {

	public static final List<Item> SUPPORTED_CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

	public static final List<MixingRecipe> ALL = createRecipes();
	public static final Map<Item, List<MixingRecipe>> BY_ITEM = sortRecipesByItem(ALL);

	private static List<MixingRecipe> createRecipes() {
		List<MixingRecipe> mixingRecipes = new ArrayList<>();

		int recipeIndex = 0;

		List<Item> allowedSupportedContainers = new ArrayList<>();
		List<ItemStack> supportedContainerStacks = new ArrayList<>();
		for (Item container : SUPPORTED_CONTAINERS) {
			ItemStack stack = new ItemStack(container);
			supportedContainerStacks.add(stack);
			if (PotionBrewing.ALLOWED_CONTAINER.test(stack)) {
				allowedSupportedContainers.add(container);
			}
		}

		for (Item container : allowedSupportedContainers) {
			BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
			for (PotionBrewing.Mix<Potion> mix : PotionBrewing.POTION_MIXES) {
				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(mix.from.get(), bottleType, 1000);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(mix.to.get(), bottleType, 1000);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla_" + recipeIndex++, mix.ingredient, fromFluid, toFluid));
			}
		}

		for (PotionBrewing.Mix<Item> mix : PotionBrewing.CONTAINER_MIXES) {
			Item from = mix.from.get();
			if (!allowedSupportedContainers.contains(from)) {
				continue;
			}
			Item to = mix.to.get();
			if (!allowedSupportedContainers.contains(to)) {
				continue;
			}
			BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(from);
			BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(to);
			Ingredient ingredient = mix.ingredient;

			for (Potion potion : ForgeRegistries.POTIONS.getValues()) {
				if (potion == Potions.EMPTY) {
					continue;
				}

				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(potion, fromBottleType, 1000);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(potion, toBottleType, 1000);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla_" + recipeIndex++, ingredient, fromFluid, toFluid));
			}
		}

		recipeIndex = 0;
		for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
			if (recipe instanceof BrewingRecipe recipeImpl) {
				ItemStack output = recipeImpl.getOutput();
				if (!SUPPORTED_CONTAINERS.contains(output.getItem())) {
					continue;
				}

				Ingredient input = recipeImpl.getInput();
				Ingredient ingredient = recipeImpl.getIngredient();
				FluidStack outputFluid = null;
				for (ItemStack stack : supportedContainerStacks) {
					if (input.test(stack)) {
						ItemStack[] stacks = input.getItems();
						if (stacks.length == 0){
							continue;
						}
						FluidStack inputFluid = PotionFluidHandler.getFluidFromPotionItem(stacks[0]);
						inputFluid.setAmount(1000);
						if (outputFluid == null) {
							outputFluid = PotionFluidHandler.getFluidFromPotionItem(output);
						}
						outputFluid.setAmount(1000);
						mixingRecipes.add(createRecipe("potion_mixing_modded_" + recipeIndex++, ingredient, inputFluid, outputFluid));
					}
				}
			}
		}

		return mixingRecipes;
	}

	private static MixingRecipe createRecipe(String id, Ingredient ingredient, FluidStack fromFluid, FluidStack toFluid) {
		return new ProcessingRecipeBuilder<>(MixingRecipe::new,
				Create.asResource(id)).require(ingredient)
				.require(FluidIngredient.fromFluidStack(fromFluid))
				.output(toFluid)
				.requiresHeat(HeatCondition.HEATED)
				.build();
	}

	private static Map<Item, List<MixingRecipe>> sortRecipesByItem(List<MixingRecipe> all) {
		Map<Item, List<MixingRecipe>> byItem = new HashMap<>();
		Set<Item> processedItems = new HashSet<>();
		for (MixingRecipe recipe : all) {
			for (Ingredient ingredient : recipe.getIngredients()) {
				for (ItemStack itemStack : ingredient.getItems()) {
					Item item = itemStack.getItem();
					if (processedItems.add(item)) {
						byItem.computeIfAbsent(item, i -> new ArrayList<>())
							.add(recipe);
					}
				}
			}
			processedItems.clear();
		}
		return byItem;
	}

}
