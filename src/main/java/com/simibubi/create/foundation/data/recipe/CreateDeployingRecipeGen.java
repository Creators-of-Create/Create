package com.simibubi.create.foundation.data.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.DeployingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopperCollection;

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

		COPPER_BLOCK = oxidizationChain(Blocks.COPPER_BLOCK),

	COPPER_BULB = oxidizationChain(Blocks.COPPER_BULB),

	CHISELED_COPPER = oxidizationChain(Blocks.CHISELED_COPPER),

	COPPER_GRATE = oxidizationChain(Blocks.COPPER_GRATE),

	COPPER_DOOR = oxidizationChain(Blocks.COPPER_DOOR),

	COPPER_TRAPDOOR = oxidizationChain(Blocks.COPPER_TRAPDOOR),

	CUT_COPPER = oxidizationChain(Blocks.CUT_COPPER),

	CUT_COPPER_STAIRS = oxidizationChain(Blocks.CUT_COPPER_STAIRS),

	CUT_COPPER_SLAB = oxidizationChain(Blocks.CUT_COPPER_SLAB);

	private GeneratedRecipe oxidizationChain(WeatheringCopperCollection<? extends ItemLike> collection) {
		return oxidizationChain(
			List.of(collection.weathering()::unaffected, collection.weathering()::exposed,
				collection.weathering()::weathered, collection.weathering()::oxidized),
			List.of(collection.waxed()::unaffected, collection.waxed()::exposed,
				collection.waxed()::weathered, collection.waxed()::oxidized));
	}

	public CreateDeployingRecipeGen(PackOutput output, CompletableFuture<Provider> registries) {
		super(output, registries, Create.ID);
	}
}
