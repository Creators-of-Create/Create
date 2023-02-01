package com.simibubi.create.foundation.data.recipe;

import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.block.CopperBlockSet;
import com.simibubi.create.foundation.block.CopperBlockSet.Variant;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

public class DeployingRecipeGen extends ProcessingRecipeGen {

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

	public GeneratedRecipe copperChain(CopperBlockSet set) {
		for (Variant<?> variant : set.getVariants())
			for (WeatherState state : WeatherState.values())
				addWax(set.get(variant, state, true)::get, set.get(variant, state, false)::get);
		return null;
	}

	public GeneratedRecipe addWax(Supplier<ItemLike> waxed, Supplier<ItemLike> nonWaxed) {
		return createWithDeferredId(idWithSuffix(waxed, "_from_adding_wax"), b -> b.require(nonWaxed.get())
			.require(Items.HONEYCOMB_BLOCK)
			.toolNotConsumed()
			.output(waxed.get()));
	}

	public DeployingRecipeGen(PackOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.DEPLOYING;
	}

}
