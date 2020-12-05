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

public class MixingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	TEMPCOBBLE = create("temp_cobble", b -> b.require(Fluids.WATER, 250)
		.require(Fluids.LAVA, 25)
		.output(Blocks.COBBLESTONE, 1)),

		TEMP_LAVA = create("temp_lava", b -> b.require(Tags.Items.COBBLESTONE)
			.output(Fluids.LAVA, 25)
			.requiresHeat(HeatCondition.SUPERHEATED)),

		TEA = create("tea", b -> b.require(Fluids.WATER, 250)
			.require(AllTags.forgeFluidTag("milk"), 250)
			.require(ItemTags.LEAVES)
			.output(AllFluids.TEA.get(), 500)
			.requiresHeat(HeatCondition.HEATED)),
		
		CHOCOLATE = create("chocolate", b -> b
			.require(AllTags.forgeFluidTag("milk"), 250)
			.require(Items.SUGAR)
			.require(Items.COCOA_BEANS)
			.output(AllFluids.CHOCOLATE.get(), 250)
			.requiresHeat(HeatCondition.HEATED)),

		BRASS_INGOT = create("brass_ingot", b -> b.require(I.copper())
			.require(I.zinc())
			.output(AllItems.BRASS_INGOT.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		CRUSHED_BRASS = create("crushed_brass", b -> b.require(AllItems.CRUSHED_COPPER.get())
			.require(AllItems.CRUSHED_ZINC.get())
			.output(AllItems.CRUSHED_BRASS.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		GUNPOWDER = create("gunpowder", b -> b.require(ItemTags.COALS)
			.require(AllItems.CRUSHED_ZINC.get())
			.require(Items.GUNPOWDER)
			.output(Items.GUNPOWDER, 2)
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
