package com.simibubi.create.foundation.data.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.DeployingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

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

		COPPER_BLOCK = oxidizationChain(
		List.of(() -> Blocks.COPPER_BLOCK, () -> Blocks.EXPOSED_COPPER, () -> Blocks.WEATHERED_COPPER, () -> Blocks.OXIDIZED_COPPER),
		List.of(() -> Blocks.WAXED_COPPER_BLOCK, () -> Blocks.WAXED_EXPOSED_COPPER, () -> Blocks.WAXED_WEATHERED_COPPER, () -> Blocks.WAXED_OXIDIZED_COPPER)),

	COPPER_BULB = oxidizationChain(
		List.of(() -> Blocks.COPPER_BULB, () -> Blocks.EXPOSED_COPPER_BULB, () -> Blocks.WEATHERED_COPPER_BULB, () -> Blocks.OXIDIZED_COPPER_BULB),
		List.of(() -> Blocks.WAXED_COPPER_BULB, () -> Blocks.WAXED_EXPOSED_COPPER_BULB, () -> Blocks.WAXED_WEATHERED_COPPER_BULB, () -> Blocks.WAXED_OXIDIZED_COPPER_BULB)),

	CHISELED_COPPER = oxidizationChain(
		List.of(() -> Blocks.CHISELED_COPPER, () -> Blocks.EXPOSED_CHISELED_COPPER, () -> Blocks.WEATHERED_CHISELED_COPPER, () -> Blocks.OXIDIZED_CHISELED_COPPER),
		List.of(() -> Blocks.WAXED_CHISELED_COPPER, () -> Blocks.WAXED_EXPOSED_CHISELED_COPPER, () -> Blocks.WAXED_WEATHERED_CHISELED_COPPER, () -> Blocks.WAXED_OXIDIZED_CHISELED_COPPER)),

	COPPER_GRATE = oxidizationChain(
		List.of(() -> Blocks.COPPER_GRATE, () -> Blocks.EXPOSED_COPPER_GRATE, () -> Blocks.WEATHERED_COPPER_GRATE, () -> Blocks.OXIDIZED_COPPER_GRATE),
		List.of(() -> Blocks.WAXED_COPPER_GRATE, () -> Blocks.WAXED_EXPOSED_COPPER_GRATE, () -> Blocks.WAXED_WEATHERED_COPPER_GRATE, () -> Blocks.WAXED_OXIDIZED_COPPER_GRATE)),

	COPPER_DOOR = oxidizationChain(
		List.of(() -> Blocks.COPPER_DOOR, () -> Blocks.EXPOSED_COPPER_DOOR, () -> Blocks.WEATHERED_COPPER_DOOR, () -> Blocks.OXIDIZED_COPPER_DOOR),
		List.of(() -> Blocks.WAXED_COPPER_DOOR, () -> Blocks.WAXED_EXPOSED_COPPER_DOOR, () -> Blocks.WAXED_WEATHERED_COPPER_DOOR, () -> Blocks.WAXED_OXIDIZED_COPPER_DOOR)),

	COPPER_TRAPDOOR = oxidizationChain(
		List.of(() -> Blocks.COPPER_TRAPDOOR, () -> Blocks.EXPOSED_COPPER_TRAPDOOR, () -> Blocks.WEATHERED_COPPER_TRAPDOOR, () -> Blocks.OXIDIZED_COPPER_TRAPDOOR),
		List.of(() -> Blocks.WAXED_COPPER_TRAPDOOR, () -> Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, () -> Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, () -> Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR)),

	CUT_COPPER = oxidizationChain(
		List.of(() -> Blocks.CUT_COPPER, () -> Blocks.EXPOSED_CUT_COPPER, () -> Blocks.WEATHERED_CUT_COPPER, () -> Blocks.OXIDIZED_CUT_COPPER),
		List.of(() -> Blocks.WAXED_CUT_COPPER, () -> Blocks.WAXED_EXPOSED_CUT_COPPER, () -> Blocks.WAXED_WEATHERED_CUT_COPPER, () -> Blocks.WAXED_OXIDIZED_CUT_COPPER)),

	CUT_COPPER_STAIRS = oxidizationChain(
		List.of(() -> Blocks.CUT_COPPER_STAIRS, () -> Blocks.EXPOSED_CUT_COPPER_STAIRS, () -> Blocks.WEATHERED_CUT_COPPER_STAIRS, () -> Blocks.OXIDIZED_CUT_COPPER_STAIRS),
		List.of(() -> Blocks.WAXED_CUT_COPPER_STAIRS, () -> Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, () -> Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, () -> Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS)),

	CUT_COPPER_SLAB = oxidizationChain(
		List.of(() -> Blocks.CUT_COPPER_SLAB, () -> Blocks.EXPOSED_CUT_COPPER_SLAB, () -> Blocks.WEATHERED_CUT_COPPER_SLAB, () -> Blocks.OXIDIZED_CUT_COPPER_SLAB),
		List.of(() -> Blocks.WAXED_CUT_COPPER_SLAB, () -> Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, () -> Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, () -> Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB));

	public CreateDeployingRecipeGen(PackOutput output, CompletableFuture<Provider> registries) {
		super(output, registries, Create.ID);
	}
}
