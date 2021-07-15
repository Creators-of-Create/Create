package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.processing.HeatCondition;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.GeneratedRecipe;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

public class MixingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	TEMP_LAVA = create("lava_from_cobble", b -> b.require(Tags.Items.COBBLESTONE)
		.output(Fluids.LAVA, 50)
		.requiresHeat(HeatCondition.SUPERHEATED)),

		TEA = create("tea", b -> b.require(Fluids.WATER, 250)
			.require(Tags.Fluids.MILK, 250)
			.require(ItemTags.LEAVES)
			.output(AllFluids.TEA.get(), 500)
			.requiresHeat(HeatCondition.HEATED)),

		CHOCOLATE = create("chocolate", b -> b.require(Tags.Fluids.MILK, 250)
			.require(Items.SUGAR)
			.require(Items.COCOA_BEANS)
			.output(AllFluids.CHOCOLATE.get(), 250)
			.requiresHeat(HeatCondition.HEATED)),

		CHOCOLATE_MELTING = create("chocolate_melting", b -> b.require(AllItems.BAR_OF_CHOCOLATE.get())
			.output(AllFluids.CHOCOLATE.get(), 250)
			.requiresHeat(HeatCondition.HEATED)),

		HONEY = create("honey", b -> b.require(Items.HONEY_BLOCK)
			.output(AllFluids.HONEY.get(), 1000)
			.requiresHeat(HeatCondition.HEATED)),

		DOUGH = create("dough_by_mixing", b -> b.require(AllItems.WHEAT_FLOUR.get())
			.require(Fluids.WATER, 1000)
			.output(AllItems.DOUGH.get(), 1)),

		BRASS_INGOT = create("brass_ingot", b -> b.require(I.copper())
			.require(I.zinc())
			.output(AllItems.BRASS_INGOT.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		CRUSHED_BRASS = create("crushed_brass", b -> b.require(AllItems.CRUSHED_COPPER.get())
			.require(AllItems.CRUSHED_ZINC.get())
			.output(AllItems.CRUSHED_BRASS.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		CHROMATIC_COMPOUND = create("chromatic_compound", b -> b.require(Tags.Items.DUSTS_GLOWSTONE)
			.require(Tags.Items.DUSTS_GLOWSTONE)
			.require(Tags.Items.DUSTS_GLOWSTONE)
			.require(AllItems.POWDERED_OBSIDIAN.get())
			.require(AllItems.POWDERED_OBSIDIAN.get())
			.require(AllItems.POWDERED_OBSIDIAN.get())
			.require(AllItems.POLISHED_ROSE_QUARTZ.get())
			.output(AllItems.CHROMATIC_COMPOUND.get(), 1)
			.requiresHeat(HeatCondition.SUPERHEATED)),

		ANDESITE_ALLOY = create("andesite_alloy", b -> b.require(Blocks.ANDESITE)
			.require(AllTags.forgeItemTag("nuggets/iron"))
			.output(I.andesite(), 1)),

		ANDESITE_ALLOY_FROM_ZINC = create("andesite_alloy_from_zinc", b -> b.require(Blocks.ANDESITE)
			.require(I.zincNugget())
			.output(I.andesite(), 1))

	;

	public MixingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MIXING;
	}

}
