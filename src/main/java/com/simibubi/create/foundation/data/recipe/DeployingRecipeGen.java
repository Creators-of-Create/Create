package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe.Builder;
import com.simibubi.create.foundation.block.CopperBlockSet;
import com.simibubi.create.foundation.block.CopperBlockSet.Variant;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

public class DeployingRecipeGen extends ProcessingRecipeGen<ItemApplicationRecipe.Builder<DeployerApplicationRecipe>> {

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
			List.of(() -> Blocks.WAXED_CUT_COPPER_SLAB, () -> Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, () -> Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, () -> Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB))

		;

	public GeneratedRecipe copperChain(CopperBlockSet set) {
		for (Variant<?> variant : set.getVariants()) {
			List<Supplier<ItemLike>> chain = new ArrayList<>(4);
			List<Supplier<ItemLike>> waxedChain = new ArrayList<>(4);

			for (WeatherState state : WeatherState.values()) {
				waxedChain.add(set.get(variant, state, true)::get);
				chain.add(set.get(variant, state, false)::get);
			}

			oxidizationChain(chain, waxedChain);
		}
		return null;
	}

	public GeneratedRecipe oxidizationChain(List<Supplier<ItemLike>> chain, List<Supplier<ItemLike>> waxedChain) {
		for (int i = 0; i < chain.size() - 1; i++) {
			Supplier<ItemLike> to = chain.get(i);
			Supplier<ItemLike> from = chain.get(i + 1);
			createWithDeferredId(idWithSuffix(to, "_from_deoxidising"), b -> b.require(from.get())
				.require(ItemTags.AXES)
				.toolNotConsumed()
				.output(to.get()));
		}

		for (int i = 0; i < chain.size(); i++)
			addWax(waxedChain.get(i), chain.get(i));

		return null;
	}

	public GeneratedRecipe addWax(Supplier<ItemLike> waxed, Supplier<ItemLike> nonWaxed) {
		createWithDeferredId(idWithSuffix(nonWaxed, "_from_removing_wax"), b -> b.require(waxed.get())
			.require(ItemTags.AXES)
			.toolNotConsumed()
			.output(nonWaxed.get()));

		return createWithDeferredId(idWithSuffix(waxed, "_from_adding_wax"), b -> b.require(nonWaxed.get())
			.require(Items.HONEYCOMB_BLOCK)
			.toolNotConsumed()
			.output(waxed.get()));
	}

	public DeployingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.DEPLOYING;
	}

	@Override
	protected Builder<DeployerApplicationRecipe> getBuilder(ResourceLocation id) {
		return new Builder<>(DeployerApplicationRecipe::new, id);
	}

}
