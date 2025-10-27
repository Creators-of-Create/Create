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
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;
import com.simibubi.create.foundation.mixin.accessor.PotionBrewingAccessor;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class PotionMixingRecipes {

	public static final List<Item> SUPPORTED_CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

	private static List<RecipeHolder<MixingRecipe>> RECIPES;
	private static Map<Item, List<MixingRecipe>> SORTED;

	private static boolean alreadyGenerated = false;
	private static boolean alreadySorted = false;

	public static List<RecipeHolder<MixingRecipe>> createRecipes(Level level) {
		if (!alreadyGenerated) {
			RECIPES = createRecipesImpl(level);
			alreadyGenerated = true;
		}

		return RECIPES;
	}

	public static Map<Item, List<MixingRecipe>> sortRecipesByItem(Level level) {
		if (!alreadySorted) {
			SORTED = sortRecipesByItem(createRecipes(level));
			alreadySorted = true;
		}

		return SORTED;
	}

	private static List<RecipeHolder<MixingRecipe>> createRecipesImpl(Level level) {
		PotionBrewing potionBrewing = level.potionBrewing();

		List<RecipeHolder<MixingRecipe>> mixingRecipes = new ArrayList<>();

		int recipeIndex = 0;

		List<Item> allowedSupportedContainers = new ArrayList<>();
		List<ItemStack> supportedContainerStacks = new ArrayList<>();
		for (Item container : SUPPORTED_CONTAINERS) {
			ItemStack stack = new ItemStack(container);
			supportedContainerStacks.add(stack);
			if (((PotionBrewingAccessor) potionBrewing).create$isContainer(stack)) {
				allowedSupportedContainers.add(container);
			}
		}

		for (Item container : allowedSupportedContainers) {
			BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
			for (PotionBrewing.Mix<Potion> mix : ((PotionBrewingAccessor) potionBrewing).create$getPotionMixes()) {
				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.from()), bottleType, 1000);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.to()), bottleType, 1000);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla_" + recipeIndex++, mix.ingredient(), fromFluid, toFluid));
			}
		}

		for (PotionBrewing.Mix<Item> mix : ((PotionBrewingAccessor) potionBrewing).create$getContainerMixes()) {
			Item from = mix.from().value();
			if (!allowedSupportedContainers.contains(from)) {
				continue;
			}
			Item to = mix.to().value();
			if (!allowedSupportedContainers.contains(to)) {
				continue;
			}
			BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(from);
			BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(to);
			Ingredient ingredient = mix.ingredient();

			List<Reference<Potion>> potions = level.registryAccess()
				.lookupOrThrow(Registries.POTION)
				.listElements()
				.toList();

			for (Reference<Potion> potion : potions) {
				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), fromBottleType, 1000);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), toBottleType, 1000);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla_" + recipeIndex++, ingredient, fromFluid, toFluid));
			}
		}

		recipeIndex = 0;
		for (IBrewingRecipe recipe : potionBrewing.getRecipes()) {
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

	private static RecipeHolder<MixingRecipe> createRecipe(String id, Ingredient ingredient, FluidStack fromFluid, FluidStack toFluid) {
		ResourceLocation recipeId = Create.asResource(id);
		MixingRecipe recipe = new Builder<>(MixingRecipe::new, recipeId)
				.require(ingredient)
			.require(SizedFluidIngredient.of(fromFluid))
				.output(toFluid)
				.requiresHeat(HeatCondition.HEATED)
				.build();

		return new RecipeHolder<>(recipeId, recipe);
	}

	private static Map<Item, List<MixingRecipe>> sortRecipesByItem(List<RecipeHolder<MixingRecipe>> all) {
		Map<Item, List<MixingRecipe>> byItem = new HashMap<>();
		Set<Item> processedItems = new HashSet<>();
		for (RecipeHolder<MixingRecipe> recipe : all) {
			for (Ingredient ingredient : recipe.value().getIngredients()) {
				for (ItemStack itemStack : ingredient.getItems()) {
					Item item = itemStack.getItem();
					if (processedItems.add(item)) {
						byItem.computeIfAbsent(item, i -> new ArrayList<>())
							.add(recipe.value());
					}
				}
			}
			processedItems.clear();
		}
		return byItem;
	}

}
