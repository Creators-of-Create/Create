package com.simibubi.create.api.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.block.CopperBlockSet;
import com.simibubi.create.foundation.block.CopperBlockSet.Variant;

import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

/**
 * The base class for Deploying recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * or the helper methods contained in this class to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateDeployingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class DeployingRecipeGen extends ProcessingRecipeGen {

	public GeneratedRecipe copperChain(CopperBlockSet set) {
		for (Variant<?> variant : set.getVariants()) {
			List<Supplier<ItemLike>> chain = new ArrayList<>(4);

			for (WeatherState state : WeatherState.values()) {
				addWax(set.get(variant, state, true)::get, set.get(variant, state, false)::get);
				chain.add(set.get(variant, state, false)::get);
			}

			oxidizationChain(chain);
		}
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

	public GeneratedRecipe oxidizationChain(List<Supplier<ItemLike>> chain) {
		for (int i = 0; i < chain.size() - 1; i++) {
			Supplier<ItemLike> to = chain.get(i);
			Supplier<ItemLike> from = chain.get(i + 1);
			createWithDeferredId(idWithSuffix(to, "_from_deoxidising"), b -> b.require(from.get())
				.require(ItemTags.AXES)
				.toolNotConsumed()
				.output(to.get()));
		}
		return null;
	}

	public DeployingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.DEPLOYING;
	}

}
