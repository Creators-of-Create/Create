package com.simibubi.create.compat.rei.display;

import net.minecraft.world.item.crafting.CraftingRecipe;

public class MechanicalCraftingDisplay extends CreateDisplay<CraftingRecipe> {
	MechanicalCraftingDisplay(CraftingRecipe recipe, String id) {
		super(recipe, id);
	}

	public static MechanicalCraftingDisplay shaped(CraftingRecipe recipe) {
		return new MechanicalCraftingDisplay(recipe, "automatic_shaped");
	}

	public static MechanicalCraftingDisplay regular(CraftingRecipe recipe) {
		return new MechanicalCraftingDisplay(recipe, "mechanical_crafting");
	}
}
