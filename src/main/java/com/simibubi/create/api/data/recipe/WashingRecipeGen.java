package com.simibubi.create.api.data.recipe;

import java.util.function.Supplier;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.data.recipe.CompatMetals;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * The base class for Washing recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * or the helper methods contained in this class to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateWashingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class WashingRecipeGen extends ProcessingRecipeGen {

	public GeneratedRecipe convert(Block block, Block result) {
		return create(() -> block, b -> b.output(result));
	}

	public GeneratedRecipe crushedOre(ItemEntry<Item> crushed, Supplier<ItemLike> nugget, Supplier<ItemLike> secondary,
																 float secondaryChance) {
		return create(crushed::get, b -> b.output(nugget.get(), 9)
			.output(secondaryChance, secondary.get(), 1));
	}

	protected GeneratedRecipe moddedCrushedOre(ItemEntry<? extends Item> crushed, CompatMetals metal) {
		for (Mods mod : metal.getMods()) {
			String metalName = metal.getName(mod);
			ResourceLocation nugget = mod.nuggetOf(metalName);
			create(mod.getId() + "/" + crushed.getId()
					.getPath(),
				b -> b.withItemIngredients(Ingredient.of(crushed::get))
					.output(1, nugget, 9)
					.whenModLoaded(mod.getId()));
		}
		return null;
	}

	protected GeneratedRecipe simpleModded(DatagenMod mod, String input, String output) {
		return create(mod.getId() + "/" + output, b -> b.require(mod, input)
			.output(mod, output).whenModLoaded(mod.getId()));
	}

	public WashingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.SPLASHING;
	}

}
