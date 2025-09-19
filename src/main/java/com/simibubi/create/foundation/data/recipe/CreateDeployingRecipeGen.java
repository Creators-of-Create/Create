package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.DeployingRecipeGen;

import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Create's own Data Generation for Deploying recipes
 * @see DeployingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateDeployingRecipeGen extends DeployingRecipeGen {

	GeneratedRecipe COPPER_TILES = copperChain(AllBlocks.COPPER_TILES);
	GeneratedRecipe COPPER_SHINGLES = copperChain(AllBlocks.COPPER_SHINGLES);

	GeneratedRecipe

	COGWHEEL = create("cogwheel", b -> b.require(I.shaft())
		.require(I.planks())
		.output(I.cog())),

	LARGE_COGWHEEL = create("large_cogwheel", b -> b.require(I.cog())
		.require(I.planks())
		.output(I.largeCog()));

	GeneratedRecipe

	CB1 = addWax(() -> Blocks.WAXED_COPPER_BLOCK, () -> Blocks.COPPER_BLOCK),
	CB2 = addWax(() -> Blocks.WAXED_EXPOSED_COPPER, () -> Blocks.EXPOSED_COPPER),
	CB3 = addWax(() -> Blocks.WAXED_WEATHERED_COPPER, () -> Blocks.WEATHERED_COPPER),
	CB4 = addWax(() -> Blocks.WAXED_OXIDIZED_COPPER, () -> Blocks.OXIDIZED_COPPER),

	CCB1 = addWax(() -> Blocks.WAXED_CUT_COPPER, () -> Blocks.CUT_COPPER),
	CCB2 = addWax(() -> Blocks.WAXED_EXPOSED_CUT_COPPER, () -> Blocks.EXPOSED_CUT_COPPER),
	CCB3 = addWax(() -> Blocks.WAXED_WEATHERED_CUT_COPPER, () -> Blocks.WEATHERED_CUT_COPPER),
	CCB4 = addWax(() -> Blocks.WAXED_OXIDIZED_CUT_COPPER, () -> Blocks.OXIDIZED_CUT_COPPER),

	CCST1 = addWax(() -> Blocks.WAXED_CUT_COPPER_STAIRS, () -> Blocks.CUT_COPPER_STAIRS),
	CCST2 = addWax(() -> Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, () -> Blocks.EXPOSED_CUT_COPPER_STAIRS),
	CCST3 = addWax(() -> Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, () -> Blocks.WEATHERED_CUT_COPPER_STAIRS),
	CCST4 = addWax(() -> Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, () -> Blocks.OXIDIZED_CUT_COPPER_STAIRS),

	CCS1 = addWax(() -> Blocks.WAXED_CUT_COPPER_SLAB, () -> Blocks.CUT_COPPER_SLAB),
	CCS2 = addWax(() -> Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, () -> Blocks.EXPOSED_CUT_COPPER_SLAB),
	CCS3 = addWax(() -> Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, () -> Blocks.WEATHERED_CUT_COPPER_SLAB),
	CCS4 = addWax(() -> Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, () -> Blocks.OXIDIZED_CUT_COPPER_SLAB);

	GeneratedRecipe

	CB_OX = oxidizationChain(List.of(() -> Blocks.COPPER_BLOCK, () -> Blocks.EXPOSED_COPPER, () -> Blocks.WEATHERED_COPPER, () -> Blocks.OXIDIZED_COPPER)),
	CCB_OX = oxidizationChain(List.of(() -> Blocks.CUT_COPPER, () -> Blocks.EXPOSED_CUT_COPPER, () -> Blocks.WEATHERED_CUT_COPPER, () -> Blocks.OXIDIZED_CUT_COPPER)),
	CCST_OX = oxidizationChain(List.of(() -> Blocks.CUT_COPPER_STAIRS, () -> Blocks.EXPOSED_CUT_COPPER_STAIRS, () -> Blocks.WEATHERED_CUT_COPPER_STAIRS, () -> Blocks.OXIDIZED_CUT_COPPER_STAIRS)),
	CCS_OX = oxidizationChain(List.of(() -> Blocks.CUT_COPPER_SLAB, () -> Blocks.EXPOSED_CUT_COPPER_SLAB, () -> Blocks.WEATHERED_CUT_COPPER_SLAB, () -> Blocks.OXIDIZED_CUT_COPPER_SLAB));

	public CreateDeployingRecipeGen(PackOutput output) {
		super(output, Create.ID);
	}
}
