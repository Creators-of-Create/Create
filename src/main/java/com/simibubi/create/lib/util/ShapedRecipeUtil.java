package com.simibubi.create.lib.util;

import com.simibubi.create.lib.util.Constants.Crafting;

public class ShapedRecipeUtil {
	public static void setCraftingSize(int width, int height) {
		Crafting.HEIGHT = height;
		Crafting.WIDTH = width;
	}
}
