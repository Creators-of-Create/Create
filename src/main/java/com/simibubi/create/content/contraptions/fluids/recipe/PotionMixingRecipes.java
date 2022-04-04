package com.simibubi.create.content.contraptions.fluids.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.PotionBrewing$MixAccessor;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.PotionBrewingAccessor;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.tropheusj.milk.mixin.BrewingRecipeRegistryMixin;
import me.shedaniel.rei.plugin.common.displays.brewing.BrewingRecipe;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

import org.lwjgl.system.CallbackI.P;

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
			if (PotionBrewingAccessor.port_lib$ALLOWED_CONTAINER().test(stack)) {
				allowedSupportedContainers.add(container);
			}
		}

		for (Item container : allowedSupportedContainers) {
			BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
			for (PotionBrewing.Mix<Potion> mix : PotionBrewingAccessor.port_lib$POTION_MIXES()) {
				PotionBrewing$MixAccessor<Potion> access = (PotionBrewing$MixAccessor<Potion>) mix;
				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(access.port_lib$from(), bottleType, FluidConstants.BUCKET);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(access.port_lib$to(), bottleType, FluidConstants.BUCKET);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla_" + recipeIndex++, access.port_lib$ingredient(), fromFluid, toFluid));
			}
		}

		for (PotionBrewing.Mix<Item> mix : PotionBrewingAccessor.port_lib$CONTAINER_MIXES()) {
			PotionBrewing$MixAccessor<Item> access = (PotionBrewing$MixAccessor<Item>) mix;
			Item from = access.port_lib$from();
			if (!allowedSupportedContainers.contains(from)) {
				continue;
			}
			Item to = access.port_lib$to();
			if (!allowedSupportedContainers.contains(to)) {
				continue;
			}
			BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(from);
			BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(to);
			Ingredient ingredient = access.port_lib$ingredient();

			for (Entry<ResourceKey<Potion>, Potion> entry : Registry.POTION.entrySet()) {
				Potion potion = entry.getValue();
				if (potion == Potions.EMPTY) {
					continue;
				}

				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(potion, fromBottleType, FluidConstants.BUCKET);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(potion, toBottleType, FluidConstants.BUCKET);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla_" + recipeIndex++, ingredient, fromFluid, toFluid));
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
