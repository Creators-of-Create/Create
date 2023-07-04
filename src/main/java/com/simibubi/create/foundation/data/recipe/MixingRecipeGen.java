package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;

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

		DOUGH = create("dough_by_mixing", b -> b.require(I.wheatFlour())
			.require(Fluids.WATER, 1000)
			.output(AllItems.DOUGH.get(), 1)),

		BRASS_INGOT = create("brass_ingot", b -> b.require(I.copper())
			.require(I.zinc())
			.output(AllItems.BRASS_INGOT.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		ANDESITE_ALLOY = create("andesite_alloy", b -> b.require(Blocks.ANDESITE)
			.require(I.ironNugget())
			.output(I.andesite(), 1)),

		ANDESITE_ALLOY_FROM_ZINC = create("andesite_alloy_from_zinc", b -> b.require(Blocks.ANDESITE)
			.require(I.zincNugget())
			.output(I.andesite(), 1)),

		// AE2

		AE2_FLUIX = create(Mods.AE2.recipeId("fluix_crystal"), b -> b.require(Tags.Items.DUSTS_REDSTONE)
				.require(Fluids.WATER, 250)
				.require(Mods.AE2, "charged_certus_quartz_crystal")
				.require(Tags.Items.GEMS_QUARTZ)
				.output(1f, Mods.AE2, "fluix_crystal", 2)
				.whenModLoaded(Mods.AE2.getId()))

	;

	public MixingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MIXING;
	}

}
