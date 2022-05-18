package com.simibubi.create.foundation.data.recipe;

import java.util.function.Supplier;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllItemTags;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ItemApplicationRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe ANDESITE = woodCasing("andesite", I::andesite, I::andesiteCasing);
	GeneratedRecipe COPPER = woodCasingTag("copper", I::copperSheet, I::copperCasing);
	GeneratedRecipe BRASS = woodCasingTag("brass", I::brassSheet, I::brassCasing);
	GeneratedRecipe RAILWAY = create("railway_casing", b -> b.require(I.brassCasing())
		.require(I.reinforcedSheet())
		.output(I.railwayCasing()));

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

	public ItemApplicationRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.ITEM_APPLICATION;
	}

}
