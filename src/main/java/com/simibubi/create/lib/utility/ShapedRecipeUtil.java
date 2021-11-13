package com.simibubi.create.lib.utility;

import com.simibubi.create.lib.extensions.ShapedRecipeExtensions;

import net.minecraft.world.item.crafting.ShapedRecipe;

public class ShapedRecipeUtil {
	public static void setCraftingSize(int width, int height) {
		// non-static method used to set static fields because Mixin™
		// this may crash violently
		ShapedRecipe recipe = new ShapedRecipe(null ,null, 0, 0, null, null);
		((ShapedRecipeExtensions) recipe).setCraftingSize(width, height);
	}
}
