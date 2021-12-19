package com.simibubi.create.lib.utility;

import com.simibubi.create.lib.utility.Constants.Crafting;

public class ShapedRecipeUtil {
	public static void setCraftingSize(int width, int height) {
		Crafting.HEIGHT = height;
		Crafting.WIDTH = width;
	}
}
