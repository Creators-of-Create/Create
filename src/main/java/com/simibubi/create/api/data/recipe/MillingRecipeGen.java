package com.simibubi.create.api.data.recipe;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;

import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

import java.util.List;

/**
 * The base class for Milling recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * or the helper methods contained in this class to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateMillingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class MillingRecipeGen extends ProcessingRecipeGen {

	/**
	 * @deprecated poor API. Requires an ItemEntry, and uses a string to create a tag. Unused by Create.
	 */
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	protected GeneratedRecipe metalOre(String name, ItemEntry<? extends Item> crushed, int duration) {
		return create(name + "_ore", b -> b.duration(duration)
			.withCondition(new NotCondition(new TagEmptyCondition("forge", "ores/" + name)))
			.require(AllTags.forgeItemTag("ores/" + name))
			.output(crushed.get()));
	}

	/**
	 * Generates a milling recipe for a modded item with outputs that are registered.
	 * Chances, outputs, and output counts are linked by index: lists should have a matching size from 1-4.
	 * @param mod Mod the input is from.
	 * @param input Item name.
	 * @param chances Chance for each output.
	 * @param dyes Output items.
	 * @param amounts Maximum output counts for each output.
	 * @return The recipe.
	 */
	protected GeneratedRecipe modFlower(DatagenMod mod, String input, List<Float> chances,
							  List<Item> dyes, List<Integer> amounts){
		return switch (chances.size()) {
			// Milling recipe has a max of 4 outputs
			case 1 -> create(mod.recipeId(input), b -> b.duration(50)
				.require(mod, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.whenModLoaded(mod.getId()));
			case 2 -> create(mod.recipeId(input), b -> b.duration(50)
				.require(mod, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.whenModLoaded(mod.getId()));
			case 3 -> create(mod.recipeId(input), b -> b.duration(50)
				.require(mod, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.output(chances.get(2), dyes.get(2), amounts.get(2))
				.whenModLoaded(mod.getId()));
			case 4 -> create(mod.recipeId(input), b -> b.duration(50)
				.require(mod, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.output(chances.get(2), dyes.get(2), amounts.get(2))
				.output(chances.get(3), dyes.get(3), amounts.get(3))
				.whenModLoaded(mod.getId()));
			default -> null;
		};
	}

	public MillingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MILLING;
	}

}
