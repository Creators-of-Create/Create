package com.simibubi.create.compat.jei.display;

import net.minecraft.world.item.crafting.CraftingRecipe;

public class MechanicalCraftingDisplay extends AbstractCreateDisplay<CraftingRecipe> {
	public MechanicalCraftingDisplay(CraftingRecipe recipe) {
		super(recipe, "mechanical_crafting");
	}
}
