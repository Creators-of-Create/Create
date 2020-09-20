package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;

public class CuttingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	ANDESITE_ALLOY = create(I::andesite, b -> b.duration(200)
		.output(AllBlocks.SHAFT.get(), 6)),

		OAK_WOOD = stripAndMakePlanks(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_PLANKS),
		SPRUCE_WOOD = stripAndMakePlanks(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_PLANKS),
		BIRCH_WOOD = stripAndMakePlanks(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_PLANKS),
		JUNGLE_WOOD = stripAndMakePlanks(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_PLANKS),
		ACACIA_WOOD = stripAndMakePlanks(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.ACACIA_PLANKS),
		DARK_OAK_WOOD = stripAndMakePlanks(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.DARK_OAK_PLANKS),

		OAK_LOG = stripAndMakePlanks(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_PLANKS),
		SPRUCE_LOG = stripAndMakePlanks(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS),
		BIRCH_LOG = stripAndMakePlanks(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_PLANKS),
		JUNGLE_LOG = stripAndMakePlanks(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_PLANKS),
		ACACIA_LOG = stripAndMakePlanks(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_PLANKS),
		DARK_OAK_LOG = stripAndMakePlanks(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS)

	;

	GeneratedRecipe stripAndMakePlanks(Block wood, Block stripped, Block planks) {
		create(() -> wood, b -> b.duration(50)
			.output(stripped));
		return create(() -> stripped, b -> b.duration(100)
			.output(planks, 5));
	}

	public CuttingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CUTTING;
	}

}
