package com.simibubi.create.api.data.recipe;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;

import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

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

	protected GeneratedRecipe moddedSandstone(DatagenMod mod, String name) {
		String sandstone = name + "_sandstone";
		return create(mod.recipeId(sandstone), b -> b.duration(150)
			.require(mod, sandstone)
			.output(mod, name + "_sand")
			.whenModLoaded(mod.getId()));
	}

	public MillingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MILLING;
	}

}
