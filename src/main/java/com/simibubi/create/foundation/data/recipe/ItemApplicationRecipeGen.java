package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe.Builder;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import net.neoforged.neoforge.common.Tags;

public class ItemApplicationRecipeGen extends ProcessingRecipeGen<ItemApplicationRecipe.Builder<ManualApplicationRecipe>> {

	GeneratedRecipe BOUND_CARDBOARD_BLOCK = create("bound_cardboard_inworld",
		b -> b.require(AllBlocks.CARDBOARD_BLOCK.asItem())
			.require(Tags.Items.STRINGS)
			.output(AllBlocks.BOUND_CARDBOARD_BLOCK.asStack()));

	GeneratedRecipe ANDESITE = woodCasing("andesite", I::andesiteAlloy, I::andesiteCasing);
	GeneratedRecipe COPPER = woodCasingTag("copper", I::copper, I::copperCasing);
	GeneratedRecipe BRASS = woodCasingTag("brass", I::brass, I::brassCasing);
	GeneratedRecipe RAILWAY = create("railway_casing", b -> b.require(I.brassCasing())
		.require(I.sturdySheet())
		.output(I.railwayCasing()));

	protected GeneratedRecipe woodCasing(String type, Supplier<ItemLike> ingredient, Supplier<ItemLike> output) {
		return woodCasingIngredient(type, () -> Ingredient.of(ingredient.get()), output);
	}

	protected GeneratedRecipe woodCasingTag(String type, Supplier<TagKey<Item>> ingredient, Supplier<ItemLike> output) {
		return woodCasingIngredient(type, () -> Ingredient.of(ingredient.get()), output);
	}

	protected GeneratedRecipe woodCasingIngredient(String type, Supplier<Ingredient> ingredient,
		Supplier<ItemLike> output) {
		create(type + "_casing_from_log", b -> b.require(Tags.Items.STRIPPED_LOGS)
			.require(ingredient.get())
			.output(output.get()));
		return create(type + "_casing_from_wood", b -> b.require(Tags.Items.STRIPPED_WOODS)
			.require(ingredient.get())
			.output(output.get()));
	}

	public ItemApplicationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.ITEM_APPLICATION;
	}

	@Override
	protected Builder<ManualApplicationRecipe> getBuilder(ResourceLocation id) {
		return new Builder<>(ManualApplicationRecipe::new, id);
	}

}
