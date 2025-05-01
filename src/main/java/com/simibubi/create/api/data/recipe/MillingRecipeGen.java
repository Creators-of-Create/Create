package com.simibubi.create.api.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;

import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

@SuppressWarnings("unused")
public abstract class MillingRecipeGen extends ProcessingRecipeGen {

	protected GeneratedRecipe metalOre(String name, ItemEntry<? extends Item> crushed, int duration) {
		return create(name + "_ore", b -> b.duration(duration)
			.withCondition(new NotCondition(new TagEmptyCondition("forge", "ores/" + name)))
			.require(AllTags.forgeItemTag("ores/" + name))
			.output(crushed.get()));
	}

	public MillingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MILLING;
	}

}
