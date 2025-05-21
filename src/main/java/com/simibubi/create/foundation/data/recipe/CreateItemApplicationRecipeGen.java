package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.ItemApplicationRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.Tags;

/**
 * Create's own Data Generation for Item Application recipes
 * @see ItemApplicationRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateItemApplicationRecipeGen extends ItemApplicationRecipeGen {

	GeneratedRecipe

	BOUND_CARDBOARD_BLOCK = create("bound_cardboard_inworld",
		b -> b.require(AllBlocks.CARDBOARD_BLOCK.asItem())
			.require(Tags.Items.STRINGS)
		.output(AllBlocks.BOUND_CARDBOARD_BLOCK.asStack())),

	ANDESITE = woodCasing("andesite", CreateRecipeProvider.I::andesiteAlloy, CreateRecipeProvider.I::andesiteCasing),
	COPPER = woodCasingTag("copper", CreateRecipeProvider.I::copper, CreateRecipeProvider.I::copperCasing),
	BRASS = woodCasingTag("brass", CreateRecipeProvider.I::brass, CreateRecipeProvider.I::brassCasing),
	RAILWAY = create("railway_casing", b -> b.require(CreateRecipeProvider.I.brassCasing())
		.require(CreateRecipeProvider.I.sturdySheet())
		.output(CreateRecipeProvider.I.railwayCasing()));


	public CreateItemApplicationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}
}
