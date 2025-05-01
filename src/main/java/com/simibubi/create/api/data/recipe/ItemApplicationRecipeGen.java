package com.simibubi.create.api.data.recipe;

import java.util.function.Supplier;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllItemTags;

import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * The base class for Item Application recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * or the helper methods contained in this class to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateItemApplicationRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class ItemApplicationRecipeGen extends ProcessingRecipeGen {
	protected GeneratedRecipe woodCasing(String type, Supplier<ItemLike> ingredient, Supplier<ItemLike> output) {
		return woodCasingIngredient(type, () -> Ingredient.of(ingredient.get()), output);
	}

	protected GeneratedRecipe woodCasingTag(String type, Supplier<TagKey<Item>> ingredient, Supplier<ItemLike> output) {
		return woodCasingIngredient(type, () -> Ingredient.of(ingredient.get()), output);
	}

	protected GeneratedRecipe woodCasingIngredient(String type, Supplier<Ingredient> ingredient,
																			  Supplier<ItemLike> output) {
		create(type + "_casing_from_log", b -> b.require(AllItemTags.STRIPPED_LOGS.tag)
			.require(ingredient.get())
			.output(output.get()));
		return create(type + "_casing_from_wood", b -> b.require(AllItemTags.STRIPPED_WOOD.tag)
			.require(ingredient.get())
			.output(output.get()));
	}

	public ItemApplicationRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.ITEM_APPLICATION;
	}

}
