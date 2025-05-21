package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.CompactingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.Tags;

/**
 * Create's own Data Generation for Compacting recipes
 * @see CompactingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateCompactingRecipeGen extends CompactingRecipeGen {
	public CreateCompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}

	GeneratedRecipe

	GRANITE = create("granite_from_flint", b -> b.require(Items.FLINT)
		.require(Items.FLINT)
		.require(Fluids.LAVA, 100)
		.require(Items.RED_SAND)
		.output(Blocks.GRANITE, 1)),

	DIORITE = create("diorite_from_flint", b -> b.require(Items.FLINT)
		.require(Items.FLINT)
		.require(Fluids.LAVA, 100)
		.require(Items.CALCITE)
		.output(Blocks.DIORITE, 1)),

	ANDESITE = create("andesite_from_flint", b -> b.require(Items.FLINT)
		.require(Items.FLINT)
		.require(Fluids.LAVA, 100)
		.require(Items.GRAVEL)
		.output(Blocks.ANDESITE, 1)),

	CHOCOLATE = create("chocolate", b -> b.require(AllFluids.CHOCOLATE.get(), 250)
		.output(AllItems.BAR_OF_CHOCOLATE.get(), 1)),

	BLAZE_CAKE = create("blaze_cake", b -> b.require(Tags.Items.EGGS)
		.require(Items.SUGAR)
		.require(AllItems.CINDER_FLOUR.get())
		.output(AllItems.BLAZE_CAKE_BASE.get(), 1)),

	HONEY = create("honey", b -> b.require(Tags.Fluids.HONEY, 1000)
		.output(Items.HONEY_BLOCK, 1)),

	ICE = create("ice", b -> b
		.require(Blocks.SNOW_BLOCK).require(Blocks.SNOW_BLOCK).require(Blocks.SNOW_BLOCK)
		.require(Blocks.SNOW_BLOCK).require(Blocks.SNOW_BLOCK).require(Blocks.SNOW_BLOCK)
		.require(Blocks.SNOW_BLOCK).require(Blocks.SNOW_BLOCK).require(Blocks.SNOW_BLOCK)
		.output(Blocks.ICE))

		;
}
